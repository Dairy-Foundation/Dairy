use jni::{
    objects::JClass,
    sys::{jboolean, jint},
    JNIEnv,
};

mod core;

#[no_mangle]
#[tokio::main]
pub async extern "system" fn Java_dev_frozenmilk_dairy_milkman_MilkMan_startRouter<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    port: jint,
) {
    unsafe {
        if core::ALIVE {
            return;
        }
        core::ALIVE = true;
    }
    core::start_router(port).await;
}

#[no_mangle]
extern "system" fn Java_dev_frozenmilk_dairy_milkman_MilkMan_isAlive<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jboolean {
    unsafe { core::ALIVE.into() }
}
