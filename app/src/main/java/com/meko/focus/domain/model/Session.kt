package com.meko.focus.domain.model

/**
 * 会话类型枚举
 *
 * 区分当前会话的类型，用于 UI 显示和业务逻辑判断。
 */
enum class SessionType {
    /** 专注会话 */
    FOCUS,

    /** 短休息会话（通常 5 分钟） */
    SHORT_BREAK,

    /** 长休息会话（通常 15 分钟，每 4 个专注周期后触发） */
    LONG_BREAK
}

/**
 * 计时器状态枚举
 *
 * 描述计时器的当前运行状态。
 */
enum class TimerState {
    /** 已停止 */
    STOPPED,

    /** 运行中 */
    RUNNING,

    /** 已暂停 */
    PAUSED
}