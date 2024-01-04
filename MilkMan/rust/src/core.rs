use core::panic;
use std::ops::ControlFlow;

use axum::{
    extract::ws::{self, WebSocket, WebSocketUpgrade},
    http::{header, HeaderMap, StatusCode},
    response::IntoResponse,
    routing::get,
    Router,
};
use futures::{SinkExt, StreamExt};
use maud::{html, Markup, DOCTYPE};
use serde_json::Value;
use tokio::signal::{self};
use tokio_tungstenite::tungstenite::{self};

mod robot_controller;

pub static mut ALIVE: bool = false;

//#[derive(Debug)]
//struct AppState {
//    broadcast_channel: broadcast::Sender<String>,
//}

pub async fn start_router<'local>(port: i32) {
    //let (broadcast_channel, _) = broadcast::channel(100);

    //let app_state = Arc::new(AppState { broadcast_channel });

    // js file system
    let fs = Router::new()
        .route("/htmx.min.js", get(serve_js_htmx))
        .route("/twind.style.min.js", get(serve_js_twind))
        .route("/ws.htmx.min.js", get(serve_js_ws));

    // internal server redirect system
    let robot_redirect = Router::new()
        //.route(
        //    "/*redirect",
        //    get(redirect_internal_robot).post(redirect_internal_robot),
        .route("/", get(internal_websocket_handler));
    //.with_state(app_state);

    // final router
    let app = Router::new()
        // nest internals
        .nest("/js", fs)
        .nest("/internal", robot_redirect)
        // nest endpoints
        .nest(
            "/robot-controller",
            robot_controller::robot_controller_router(),
        )
        // declare fallback to err404 if we haven't defined a route for the request
        .fallback(fallback_404);

    axum::Server::bind(&format!("0.0.0.0:{port}").parse().unwrap())
        .serve(app.into_make_service())
        .with_graceful_shutdown(shutdown_signal())
        .await
        .unwrap()
}

async fn serve_js_htmx() -> impl IntoResponse {
    let mut headers = HeaderMap::new();
    headers.insert(
        header::CONTENT_TYPE,
        "text/javascript; charset=utf-8".parse().unwrap(),
    );

    (headers, std::include_str!("../assets/htmx.min.js"))
}

async fn serve_js_twind() -> impl IntoResponse {
    let mut headers = HeaderMap::new();
    headers.insert(
        header::CONTENT_TYPE,
        "text/javascript; charset=utf-8".parse().unwrap(),
    );

    (headers, std::include_str!("../assets/twind.style.min.js"))
}

async fn serve_js_ws() -> impl IntoResponse {
    let mut headers = HeaderMap::new();
    headers.insert(
        header::CONTENT_TYPE,
        "text/javascript; charset=utf-8".parse().unwrap(),
    );

    (headers, std::include_str!("../assets/ws.htmx.min.js"))
}

async fn internal_websocket_handler(
    ws: WebSocketUpgrade,
    //State(state): State<Arc<AppState>>,
) -> impl IntoResponse {
    println!("attempting WebSocket upgrade");
    ws.on_upgrade(move |socket| internal_websocket(socket))
}

const INTERNAL_WS_ADDR: &str = "ws://192.168.43.1:8110";
async fn internal_websocket(stream: WebSocket) {
    // split so that we can do both sending and recieving at the same time
    println!("opening connection");
    let (mut client_sender, mut client_receiver) = stream.split();

    let (mut robot_sender, mut robot_receiver) =
        match tokio_tungstenite::connect_async(INTERNAL_WS_ADDR).await {
            Ok((stream, _)) => stream.split(),
            Err(_) => {
                // exits the whole thing on err connecting to the robot
                println!("couldn't establish internal connection");
                return;
            }
        };

    // ping the robot
    robot_sender
        .send(tungstenite::Message::Ping("ping!".into()))
        .await
        .expect("smth went hella wrong :skull:");

    let mut forward_from_client = tokio::spawn(async move {
        loop {
            let Some(msg) = client_receiver.next().await else {
                continue;
            };
            match msg {
                Ok(msg) => {
                    let msg = match msg {
                        ws::Message::Text(text) => Some(tungstenite::Message::text(text)),
                        ws::Message::Close(_) => break,
                        _ => None,
                    };
                    if let Some(msg) = msg {
                        robot_sender.send(msg).await.ok();
                    }
                }
                Err(err) => {
                    print!("err from client {err:?}");
                    break;
                }
            };
        }
        println!("exited from client loop")
    });

    let mut forward_from_robot = tokio::spawn(async move {
        loop {
            let Some(msg) = robot_receiver.next().await else {
                continue;
            };
            println!("recieved message from robot {msg:?}");
            let msg = match msg {
                Ok(msg) => msg,
                Err(err) => {
                    println!("err from robot {err:?}");
                    break;
                }
            };
            let processed_result = process_robot_message(msg);
            println!("processed message from robot {processed_result:?}");
            match processed_result {
                ControlFlow::Continue(opt) => {
                    if let Some(msg) = opt {
                        client_sender.send(msg).await.ok();
                        println!("forwarded message from robot");
                    };
                }
                ControlFlow::Break(_) => break,
            };
        }
        //while let Some(Ok(msg)) = robot_receiver.next().await {
        // deserialise recieved json, transform into html and forward to the client_receiver
        //}
        println!("exited from robot loop")
    });

    tokio::select! {
        _ = (&mut forward_from_client) => {
            forward_from_robot.abort();
        },
        _ = (&mut forward_from_robot) => {
            forward_from_client.abort();
        }
    }
    println!("closing middle man socket")
}

fn process_robot_message(msg: tungstenite::Message) -> ControlFlow<(), Option<ws::Message>> {
    println!("processing message {msg:?}");
    match msg {
        tungstenite::Message::Text(t) => {
            let Ok(json) = serde_json::from_str::<Value>(t.as_str()) else {
                return ControlFlow::Continue(None);
            };
            let Value::String(msg_type) = &json["type"] else {
                return ControlFlow::Continue(None);
            };
            println!("responding to message of type {msg_type}");
            let markup = match msg_type.as_str() {
                "ROBOT_STATE" => robot_controller::convert_robot_state(json),
                "RETURN_OP_MODE_META_DATA" => robot_controller::convert_op_mode_meta_data(json),
                _ => {
                    return ControlFlow::Continue(None);
                }
            };
            println!("{markup:?}");
            return ControlFlow::Continue(Some(ws::Message::Text(markup.into_string())));
        }
        tungstenite::Message::Close(_) => {
            return ControlFlow::Break(());
        }
        _ => ControlFlow::Continue(None),
    }
}

async fn fallback_404() -> impl IntoResponse {
    (StatusCode::NOT_FOUND, "404 - not found")
}

async fn shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("failed to install Ctrl+C  handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("failed to install signal handler")
            .recv()
            .await;
    };

    tokio::select! {
        _ = ctrl_c => {},
        _ = terminate => {},
    }

    println!("terminating the server");
    unsafe {
        ALIVE = false;
    }
}

pub fn doc(title: &str, inner: Markup) -> Markup {
    html!(
        (DOCTYPE)
        html {

            head {
                meta charset="utf-8";

                title { (title) }

                script type="text/javascript" src="/js/htmx.min.js" {}
                script type="text/javascript" src="/js/ws.htmx.min.js" {}
                script type="text/javascript" src="/js/twind.style.min.js" {}
            }

            body {
                (inner)
            }
        }
    )
}
