mod core;

#[tokio::main]
async fn main() {
    core::start_router(8109).await
}
