package com.honeywell.iaq.events;

/**
 * Created by H157925 on 16/5/3. 10:31
 * Email:Shodong.Sun@honeywell.com
 */
public class IAQEvents {
    public enum IAQBasicEventType {
        NORMAL_MSG_EVENT,//普通推送消息，用于Toast一下即可，处理只放在MainActivity中。
        WEBSOCKET_EVENT,
        PROGRESS_STATUS,//成功，或者失败, 并携带错误原因
        UPGRADE,//升级事件，包含升级日期，版本号，描述文字
        TIME_OUT,//超时处理
        CONNECTING_LOST,//失去连接，跳转到登录界面
        RE_LOGIN,//重新登录
    }

    public enum IAQEnvironmentDetailEventType {
        GET_SCENARIO_LIST,//获取数据库中Scenaro的列表信息
        ENABLE_SCENARIO_SUCCESS,//执行Scenario后返回结果
        EDIT_GET_DEVICE_ZONE_ARR,//三级页面中获取Scenario设备和防区

        CONFIG_SCENARIO_STATE,//Config device 消息
        CONFIG_DEVICE_LIST,//获取Device list
        CONFIG_SELECTED_DEVICES,//获取已选择的设备
        CONFIG_WI,//配置红外设备

        UPDATE_SYSTEM_SCENARIO_STATUS,//更新Scenario防护状态，用于Event事件监听

    }
    public enum IAQEnvironmentChartEventType {
        GET_SCENARIO_LIST,//获取数据库中Scenaro的列表信息
        ENABLE_SCENARIO_SUCCESS,//执行Scenario后返回结果
        EDIT_GET_DEVICE_ZONE_ARR,//三级页面中获取Scenario设备和防区

        CONFIG_SCENARIO_STATE,//Config device 消息
        CONFIG_DEVICE_LIST,//获取Device list
        CONFIG_SELECTED_DEVICES,//获取已选择的设备
        CONFIG_WI,//配置红外设备

        UPDATE_SYSTEM_SCENARIO_STATUS,//更新Scenario防护状态，用于Event事件监听

    }
    public enum IAQLoginEventType {
        LOGIN_WITH_NAME_AND_PWD,//使用用户名和密码首次登录,只有失败的时候才会发这个Event事件，通知界面失败原因
        GET_CUBE_DEVICES_SUCCESS,//获取当前链接用户的Cube 信息成功后发送
        LOGIN_WEBSOCKET_SUCCESS,//WebSocket登录成功,发送通知刷新页面
        LOGIN_SOCKET,//本地登陆
        LOGOUT,//登出
        LOGIN_REQUEST_VALID_NUM,//获取验证码，返回成功或者失败
        LOGIN_RESET_PWD,//重置密码
        LOGIN_REGISTER,//注册事件
        LOGIN_UPDATE_CONFIG,//更新数据成功
    }

    public enum CubeDeviceEventType {
        GET_DEVICE_TYPE_LIST,//从本地端获取设备类型列表,设备列表一级页面
        GET_DEVICES_FROM_DATABASE,//从本地端获取设备列表,设备列表二级页面
        UPDATE_DEVICE_STATE,//更新设备的在线状态，用于readdevice命令下,设备列表二级页面
        UPDATE_AIR_CONTROLLER_STATE,//更新空调状态

        UPDATE_BACKAUDIO_STATE_FROM_EVENT,//通过后台推送消息给前端，更新BackAudio信息

        CONFIGURE_DEVICE_STATE,//配置设备后返回状态,UI层需要做刷新数据 add 或者 edit
        CONFIGURE_DEVICE_STATE_DELETE,//配置设备后返回状态,删除动作

        DEVICE_IR_NOT_STUDY,//按键没有学习
        DEVICE_IR_STUDY,//按键学习
        DEVICE_IR_SEND,//发送按键

        DEVICE_IPC_UPDATE,//更新IPC状态

        //侧边栏 menu
        MENU_GET_ALL_DEVICE_LIST,//侧边栏获取设备页面列表
        MENU_GET_DEVICE_WITH_TYPE,//二级页面获取设备列表

        UPDATE_VENTILATION_STATUS,//更新Ventilation 状态
        UPDATE_VENTILATION_485_STATUS,//更新Ventilation 485 状态

        CALL_ELEVATOR,//呼叫电梯
    }

    public enum CubeRoomEventType {
        GET_ROOM_LIST,//首页获取房间列表
        CONFIG_ROOM_STATE,//更新房间状态，包括 删除， 增加 编辑
        UPDATE_ROOM_DEVICE_STATE,//读取房间内设备状态--显示房间内设备列表状态
    }

    public enum CubeModuleEventType {
        ADD_FIND_NEW_MODULE,//添加  发现设备 事件
        GET_MODULE_LIST,//首页 获取列表
        CONFIG_MODULE_STATE,//更新模块状态
        CONFIG_MODULE_STATE_DELETE,//删除

    }


    public enum CubeAccountEventType {
        SET_ALIASNAME,//设置名称
        SET_NEW_PWD,//设置新密码
        GET_CUBE_LIST,//获取Cube列表
        UNBIND_CUBE_RELOGIN,//对Cube进行解除绑定,跳转到登录界面
        SET_ALARM_PWD,//设置安防密码

        CUBE_SETTING,//总的Cube Setting ，用于处理错误消息
        CUBE_SETTING_NAME,//Cube setting 修改名字
        CUBE_SETTING_PWD,//Cube setting 修改密码
        CUBE_SETTING_ETHERNET,//Cube setting 设置物业网络

        CUBE_SETTING_GET_LOCATION,//定位 获取城市
        CUBE_SETTING_SET_LOCATION,//定位 设置城市
        CUBE_SETTING_UPGRADE,//手动升级到最新版本
        CUBE_SETTING_RECEIVE_UPGRADE,//收到Event事件，强制升级
        CUBE_SETTING_BACKUP,//备份事件
        CUBE_SETTING_RECOVERY,//恢复到最新版本事件
        CUBE_SETTING_VOICE_REGNIZE,//识别语音识别的标示


        CUBE_SETTING_GET_BACKUP_HISTORY,//获取备份历史
    }

    public enum CubeRuleEventType {
        GET_RULE_LIST,//获取一级页面的列表
        ENABLE_RULE,//打开关闭Rule
        CONFIG_RULE_STATE,//编辑后返回状态信息
        CONFIG_RULE_STATE_DELETE,//删除返回状态
    }

    public enum CubeScheduleEventType {
        GET_SCHEDULE_LIST,//获取一级页面的列表参数
        ENABLE_SCHEDULE,//打开关闭Schedule
        CONFIG_SCHEDULE_STATE,//更新Schedule状态 增加 编辑
        CONFIG_SCHEDULE_STATE_DELETE,//删除
    }

    public enum CubeCallEventType {
        CALL_START,//开始呼叫
        CALL_STOP,//停止呼叫
    }

    public enum CubeHeadINfoUpdateEventType {
        HEAD_ICON_CHANGE,
        OUT_SIDE_INFO_CHANGE,
        IN_SIDE_INFO_CHANGE,
    }

    public enum CubeAlarmEventType {
        GET_ALARM,//收到报警信息
        UPDATE_UNREAD_ALARM,//刷新未读条数
    }

    public enum CubeScanEventType {
        START_EASY_LINK,//弹出easy link 界面
        SCAN_CUBE_EVENT,//绑定CUBE
    }

    public enum CubeNotifiEventType {
        GET_NOTIFI_LIST_REFRESH,//获取最新的消息列表 刷新
        GET_NOTIFI_LIST_LOADMORE,//获取最新的消息列表 加载更多
        NOTIFI_PLAY_IPC_VIDEO,//IPC 播放视频
    }


}
