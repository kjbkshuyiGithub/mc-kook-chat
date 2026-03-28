# McKookChat

一个 Minecraft Fabric 模组，用于将 Minecraft 服务器聊天与 KooK（开黑啦）平台进行桥接同步。

## 功能

- 自动捕获服务器内玩家聊天消息，通过 API 供外部 Bot 拉取
- 通过 HTTP API 接收 KooK 侧消息，在游戏内以 `[KooK]` 前缀广播
- 可选 AES-256-CBC 加密保护 API 数据传输
- 轻量 REST API，方便 Python / Node.js 等外部 Bot 对接
- 使用 HaiCheng Bot 连接 Kook 频道（可使用自己编写的 Bot）

## 环境要求

| 项目       | 版本            |
|-----------|-----------------|
| Minecraft | 1.20.1          |
| Fabric Loader | >= 0.18.5   |
| Fabric API | >= 0.92.7+1.20.1 |
| Java      | >= 17           |

## 安装

1. 从 [Releases](https://github.com/kjbkshuyiGithub/mc-kook-chat/releases) 下载最新版本的 JAR 文件
2. 将 JAR 文件放入服务器的 `mods/` 目录
3. 确保已安装 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)
4. 启动服务器，模组会在 `config/` 目录下自动生成配置文件 `mc-kook-chat.json`

## 配置

配置文件：`config/mc-kook-chat.json`，首次运行自动生成。

```json
{
  "httpPort": 8888,
  "enableEncryption": true,
  "encryptionKey": "自动生成的64字符十六进制密钥",
  "maxMessages": 100,
  "kookBotAddress": ""
}
```

| 字段               | 类型    | 默认值  | 说明                                    |
|--------------------|---------|---------|-----------------------------------------|
| `httpPort`         | int     | 8888    | HTTP API 服务监听端口                    |
| `enableEncryption` | boolean | true    | 是否启用 AES-256-CBC 加密                |
| `encryptionKey`    | string  | 自动生成 | 64 字符十六进制加密密钥（首次运行自动生成）|
| `maxMessages`      | int     | 100     | 消息缓冲区最大条数，超出后丢弃最早的消息  |
| `kookBotAddress`   | string  | ""      | KooK Bot 地址（预留）                    |

## 游戏内命令

| 命令         | 权限    | 说明                                     |
|-------------|---------|------------------------------------------|
| `/kook-link` | OP >= 2 | 显示模组版本、运行状态、端口、加密状态和消息缓冲区信息 |

## API 接口

McKookChat 在服务器本地启动 HTTP 服务，提供以下 REST API：

| 方法   | 路径                  | 说明                       |
|--------|----------------------|----------------------------|
| GET    | `/api/status`        | 获取服务器运行状态           |
| GET    | `/api/messages`      | 获取聊天消息列表             |
| POST   | `/api/kook/message`  | 向 Minecraft 服务器发送消息  |

详细的接口文档、加密说明和 Python Bot 对接示例请参阅 [API.md](API.md)。

## 从源码构建

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

构建产物位于 `build/libs/` 目录。

## 工作原理

```
┌──────────────────────┐         HTTP API          ┌──────────────┐
│   Minecraft Server   │◄──────────────────────────►│   KooK Bot   │
│                      │                            │   (Python)   │
│  玩家聊天 ──► ChatInterceptor ──► ChatBuffer ──► /api/messages ──► Bot 轮询读取
│                      │                            │              │
│  [KooK] 广播 ◄── ApiKookMessageHandler ◄──── /api/kook/message ◄── Bot 发送消息
└──────────────────────┘                            └──────────────┘
```

1. **消息捕获**：通过 Fabric 事件系统 (`ServerMessageEvents.CHAT_MESSAGE`) 拦截所有玩家聊天
2. **消息存储**：写入线程安全的 `ConcurrentLinkedDeque` 缓冲区（可配置大小）
3. **消息拉取**：Bot 通过 `GET /api/messages?after=<timestamp>` 轮询获取新消息
4. **消息发送**：Bot 通过 `POST /api/kook/message` 将 KooK 消息推送到游戏内

## 许可证

[Apache-2.0](LICENSE)

## 致谢

- 作者：McBlocker
- 贡献者：shuaihanya, HaiCheng MTR Server Team
