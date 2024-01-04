use axum::{routing::get, Router};
use maud::{html, Markup};
use serde_json::Value;

use super::doc;

pub fn robot_controller_router() -> Router {
    // this declares and makes a new router, with a getter for "/" which returns the contents of
    // test_home()

    // this function gets called in core::start_router and nests it at "/robot-controller", which
    // means "/robot-controller" will now get the contents of test_home()

    // if you want to modify the core router yourself, feel free
    // otherwise I can do it
    Router::new().route("/", get(test_home))
}

async fn test_home() -> Markup {
    // the core::doc function does some templating for us, and includes all our scripts
    // automatically
    // the &str passed here determines the title of the page
    // the doc function can be changed if you want, its pretty not that deep, its just nice for
    // ensuring that the scripts get included, and provides a sample for templating
    doc(
        "MilkMan",
        html!(
            // read about the technology we are using: htmx (https://htmx.org/), with the ws extension (https://htmx.org/extensions/web-sockets/), maud (https://maud.lambda.xyz/) (the macro
            // we are using rn), and twind (https://twind.style/) (tailwind but a minified compiler
            // for it)

            // htmx has some css animation helpers, which i haven't looked at but seem nice
            // htmx also has something called hyperscript which idm adding if you want it
            // or really any other minified js file

            // this div connects to the websocket
            div hx-ext="ws" ws-connect="/internal" class="w-4/5 m-auto mt-10"{
                // and this one gets replaced by the contents of convert_robot_state(), which get
                // sent automatically by the robot, which will also notify us of any changes
                // this is a nice asyncronous way of doing robot state
                div class="block bg-gray-200" id="robot-status" {}

                select class="block bg-gray-300" id="op-mode-name" name="name" form="op-mode-init-start" {}

                // opmode initing form
                form id="op-mode-init-start" {}

                button class="bg-gray-400 ml-1" ws-send hx-vals="{\"type\": \"STOP_OP_MODE\"}" {
                    "STOP"
                }
            }
        ),
    )
}

// this is some internal logic that converts some json to html, when its coming from the robot to
// us
// feel free to modify these
// note that these get sent back through the
pub fn convert_robot_state(json: Value) -> Markup {
    // this is the process to get the right string out of the json, otherwise the results end up
    // with double quotes wrapped around them
    // unwrapping causes the process to panic, and crash the thread, which will cause the websocket
    // to restart probably (not really a big deal), and the result to never arrive
    // if you don't want this, you can use unwrap_or("default str here")

    let name = json["activeOpModeName"].as_str().unwrap();

    let state = json["opModeState"].as_str().unwrap();

    let flavour = json["flavour"].as_str().unwrap();

    html!(
        div class="block bg-gray-200" id="robot-status" hx-oob-swap="true" {
            // the " " spaces out the two strings
            (name) " " (state)
        }

        // we are sending two pieces of html back, but they aren't really connected, thanks htmx!
        @if state == "INIT" {
            form class="contents" ws-send hx-vals="{\"type\": \"START_OP_MODE\"}" id="op-mode-init-start" hx-oob-swap="true" {
                button class="bg-gray-400 mr-1" { "START" }
            }
        }
        @else if flavour != "SYSTEM" && state == "ACTIVE"  {
            form class="contents" ws-send disabled hx-vals="{\"type\": \"INIT_OP_MODE\"}" id="op-mode-init-start" hx-oob-swap="true" {
                button class="bg-gray-400 mr-1" disabled { "RUNNING" }
            }
        }
        @else {
            form class="contents" ws-send hx-vals="{\"type\": \"INIT_OP_MODE\"}" id="op-mode-init-start" hx-oob-swap="true" {
                button class="bg-gray-400 mr-1" { "INIT" }
            }
        }
    )
}

pub fn convert_op_mode_meta_data(json: Value) -> Markup {
    let flavours = json["metadata"].as_object().unwrap();

    html!(
        select class="block bg-gray-300" id="op-mode-name" name="name" hx-oob-swap="true" form="op-mode-init-start" {
            @for flavour in flavours {
                opgroup label=(flavour.0) {
                    @for meta in flavour.1.as_array().unwrap() {
                        option {
                            (meta["name"].as_str().unwrap())
                        }
                    }
                }
            }
        }
    )
}
