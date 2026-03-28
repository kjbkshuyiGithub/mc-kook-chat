# McKookChat API 文档

McKookChat 在 Minecraft 服务器本地启动一个 HTTP 服务，提供 REST API 供外部 Bot 程序对接。

- **默认地址**: `http://<服务器IP>:8888`
- **端口**: 在 `config/mc-kook-chat.json` 中通过 `httpPort` 字段配置
- **所有响应**: `Content-Type: application/json; charset=utf-8`

---

## 接口总览

| 方法   | 路径                  | 说明                         |
|--------|----------------------|------------------------------|
| GET    | `/api/status`        | 获取服务器运行状态             |
| GET    | `/api/messages`      | 获取聊天消息列表               |
| POST   | `/api/kook/message`  | 向 Minecraft 服务器发送消息    |

---

## 1. 获取服务器状态

```
GET /api/status
```

**响应示例：**

```json
{
  "modId": "mc-kook-chat",
  "version": "1.0.0",
  "running": true,
  "playerCount": 3,
  "messageCount": 42,
  "timestamp": 1714281600000
}
```

**字段说明：**

| 字段          | 类型      | 说明                              |
|---------------|----------|-----------------------------------|
| `modId`       | string   | Mod 标识符，固定为 `mc-kook-chat`  |
| `version`     | string   | Mod 版本号                         |
| `running`     | boolean  | 服务器是否正在运行                  |
| `playerCount` | int      | 当前在线玩家数                      |
| `messageCount`| int      | 消息缓冲区中的消息数量               |
| `timestamp`   | long     | 响应生成时的 Unix 时间戳（毫秒）     |

---

## 2. 获取聊天消息

```
GET /api/messages
GET /api/messages?after=<timestamp>
```

### 查询参数

| 参数    | 类型   | 必填 | 说明                                                  |
|---------|--------|------|-------------------------------------------------------|
| `after` | long   | 否   | 返回此时间戳之后的消息（Unix 毫秒），用于轮询获取新消息    |

### 未加密模式（`enableEncryption: false`）

**响应示例：**

```json
{
  "messages": [
    {
      "playerName": "Steve",
      "message": "大家好！",
      "source": "minecraft",
      "timestamp": 1714281600000
    },
    {
      "playerName": "小明",
      "message": "hello from kook",
      "source": "kook",
      "timestamp": 1714281605000
    }
  ],
  "count": 2,
  "encrypted": false
}
```

**ChatMessage 字段说明：**

| 字段         | 类型   | 说明                                              |
|-------------|--------|---------------------------------------------------|
| `playerName`| string | 发送者名称（游戏内为玩家名，KooK 为用户名）          |
| `message`   | string | 消息内容                                           |
| `source`    | string | 消息来源：`"minecraft"` 或 `"kook"`                |
| `timestamp` | long   | 消息时间戳（Unix 毫秒）                             |

### 加密模式（`enableEncryption: true`，默认）

加密模式开启时，消息数组会被 AES-256-CBC 加密后 Base64 编码放入 `data` 字段。

**响应示例：**

```json
{
  "data": "<Base64 编码的加密数据>",
  "count": 2,
  "encrypted": true
}
```

**Python 解密示例：**

```python
import base64
from Crypto.Cipher import AES  # pip install pycryptodome

def decrypt_messages(base64_data: str, hex_key: str) -> list[dict]:
    """解密 McKookChat 加密消息"""
    raw = base64.b64decode(base64_data)
    iv = raw[:16]
    ciphertext = raw[16:]
    key = bytes.fromhex(hex_key)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    decrypted = cipher.decrypt(ciphertext)
    # PKCS5Padding 去填充
    pad_len = decrypted[-1]
    decrypted = decrypted[:-pad_len]
    import json
    return json.loads(decrypted.decode("utf-8"))


# 使用示例
ENCRYPTION_KEY = "服务端 config/mc-kook-chat.json 中的 encryptionKey 字段"
data = decrypt_messages(response["data"], ENCRYPTION_KEY)
for msg in data:
    print(f"[{msg['source']}] {msg['playerName']}: {msg['message']}")
```

**加密细节：**

| 项目         | 值                          |
|-------------|-----------------------------|
| 算法         | AES-256-CBC                 |
| 密钥长度     | 256 bit（32 字节）           |
| 密钥格式     | 64 字符十六进制字符串         |
| IV           | 每次随机生成 16 字节          |
| 填充方式     | PKCS5Padding                |
| 编码方式     | IV + 密文拼接后 Base64 编码   |

---

## 3. 发送 KooK 消息到服务器

```
POST /api/kook/message
Content-Type: application/json
```

**请求体：**

```json
{
  "username": "KooK用户名",
  "content": "要发送的消息内容"
}
```

| 字段       | 类型   | 必填 | 说明                     |
|-----------|--------|------|--------------------------|
| `username` | string | 否   | 显示的用户名，默认 `"KooK"` |
| `content`  | string | 是   | 消息内容，不能为空          |

**成功响应（200）：**

```json
{
  "status": "ok",
  "message": "Message broadcast to server"
}
```

**错误响应：**

| 状态码 | 场景                     | 示例                                                     |
|-------|--------------------------|----------------------------------------------------------|
| 400   | JSON 格式无效             | `{"error": "Invalid JSON body..."}`                       |
| 400   | content 为空              | `{"error": "content field is required"}`                  |
| 405   | 非 POST 请求              | `{"error": "Method not allowed"}`                         |

**Python 调用示例：**

```python
import requests

MC_SERVER = "http://127.0.0.1:8888"

def send_kook_message(username: str, content: str) -> dict:
    """向 Minecraft 服务器广播 KooK 消息"""
    resp = requests.post(
        f"{MC_SERVER}/api/kook/message",
        json={"username": username, "content": content}
    )
    resp.raise_for_status()
    return resp.json()


# 使用示例
result = send_kook_message("小明", "大家好，我来自 KooK！")
print(result)  # {'status': 'ok', 'message': 'Message broadcast to server'}
```

---

## 4. Bot 对接完整示例（Python）

以下是一个轮询消息 + 转发消息的完整 Bot 框架：

```python
import time
import json
import base64
import requests
from Crypto.Cipher import AES

MC_SERVER = "http://127.0.0.1:8888"
ENCRYPTION_KEY = "你的encryptionKey"  # 从 config/mc-kook-chat.json 获取

# --- 解密工具 ---
def decrypt_messages(base64_data: str, hex_key: str) -> list[dict]:
    raw = base64.b64decode(base64_data)
    iv, ciphertext = raw[:16], raw[16:]
    key = bytes.fromhex(hex_key)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    decrypted = cipher.decrypt(ciphertext)
    pad_len = decrypted[-1]
    return json.loads(decrypted[:-pad_len].decode("utf-8"))

# --- API 调用 ---
def get_server_status() -> dict:
    return requests.get(f"{MC_SERVER}/api/status").json()

def poll_messages(after: int = 0) -> tuple[list[dict], int]:
    """轮询获取新消息，返回 (消息列表, 最新时间戳)"""
    resp = requests.get(f"{MC_SERVER}/api/messages", params={"after": after}).json()
    if resp.get("encrypted"):
        messages = decrypt_messages(resp["data"], ENCRYPTION_KEY)
    else:
        messages = resp.get("messages", [])
    latest = max((m["timestamp"] for m in messages), default=after)
    return messages, latest

def send_to_server(username: str, content: str) -> dict:
    resp = requests.post(
        f"{MC_SERVER}/api/kook/message",
        json={"username": username, "content": content}
    )
    resp.raise_for_status()
    return resp.json()

# --- 主循环 ---
def main():
    last_ts = 0
    print("Bot 已启动，开始轮询消息...")

    while True:
        try:
            messages, last_ts = poll_messages(last_ts)
            for msg in messages:
                if msg["source"] == "minecraft":
                    # Minecraft 消息 -> 转发到 KooK
                    print(f"[MC] {msg['playerName']}: {msg['message']}")
                    # TODO: 调用 KooK API 将消息转发到 KooK 频道

                elif msg["source"] == "kook":
                    # 来自 KooK 的消息（已被广播到服务器），跳过
                    pass

        except Exception as e:
            print(f"轮询出错: {e}")

        time.sleep(2)  # 每 2 秒轮询一次

if __name__ == "__main__":
    main()
```

**依赖安装：**

```bash
pip install requests pycryptodome
```

---

## 5. 配置说明

配置文件位置：服务器端 `config/mc-kook-chat.json`

```json
{
  "httpPort": 8888,
  "enableEncryption": true,
  "encryptionKey": "自动生成的64字符十六进制密钥",
  "maxMessages": 100,
  "kookBotAddress": ""
}
```

| 字段              | 类型    | 默认值  | 说明                                    |
|-------------------|---------|---------|-----------------------------------------|
| `httpPort`        | int     | 8888    | HTTP 服务监听端口                        |
| `enableEncryption`| boolean | true    | 是否启用 AES 加密                        |
| `encryptionKey`   | string  | 自动生成 | 64 字符十六进制密钥（首次运行自动生成）    |
| `maxMessages`     | int     | 100     | 消息缓冲区最大条数（超出后丢弃旧消息）     |
| `kookBotAddress`  | string  | ""      | KooK Bot 地址（预留字段）                 |
