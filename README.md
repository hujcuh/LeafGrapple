## English Version

### Introduction

LeafGrapple is a virtual grappling hook plugin designed for **Paper / Folia 1.21.4+**.

LeafGrapple is suitable for servers that require custom grappling hook items, modern item models, configurable movement parameters, durability, cooldown systems, and **ItemsAdder Modern graphics** support.

---

### Features

- Virtual grappling hook flight system  
- Right-click to launch the grapple  
- Right-click again to pull the player after the hook attaches to a block  
- Left-click to cancel the current grapple  
- Ray-trace-based block hit detection  
- Configurable hook head scale, offset, rotation, and glowing effects  
- Support for ItemsAdder Modern `graphics:` item models  
- Automatic compatibility with ItemsAdder `ia_auto/` model paths  
- Support for creating custom grapple types via `config.yml`  
- Configurable maximum distance, launch speed, pull acceleration, and maximum pull speed  
- Configurable cooldowns with independent cooldown overlay per grapple type  
- Custom durability support  
- Configurable sound effects  
- Multiple item recognition methods: PDC, `item_model`, and legacy CustomModelData  

---

### Permissions

- `leafgrapple.admin`

---

### Notes

- Requires **Paper / Folia 1.21.4+**  
- ItemsAdder Modern support requires **ItemsAdder 4.0.13+**  
- Configuration changes currently require a server restart  
- Currently supports grappling to blocks only  
- Grappling entities and pulling entities are not supported at this time  
- Hook heads do not automatically align to block faces yet  
- Each active grapple spawns one `ItemDisplay` entity  
- Extremely large grapple flight times or maximum distances are not recommended  

---

## 中文版
### 简介
LeafGrapple（叶子钩爪） 是一个面向 Paper / Folia 1.21.4+ 的虚拟钩爪插件。
LeafGrapple 适合需要自定义钩爪物品、现代物品模型、可配置移动参数、耐久、冷却和 ItemsAdder Modern graphics 支持的服务器。

###功能介绍

- 虚拟钩爪飞行系统
- 右键发射钩爪
- 钩爪抓住方块后再次右键拉回玩家
- 左键取消当前钩爪
- 基于射线检测的方块命中判断
- 支持配置钩头缩放、偏移、旋转和发光效果
- 支持 ItemsAdder Modern graphics: 材质
- 自动兼容 ItemsAdder ia_auto/ 模型路径
- 支持通过 config.yml 创建自定义钩爪类型
- 支持配置距离、发射速度、拉扯加速度和最大拉扯速度
- 支持配置冷却时间和每种钩爪独立冷却遮罩
- 支持自定义耐久
- 支持配置化音效
- 支持 PDC、item_model 和旧 CustomModelData 多种识别方式

### 权限
- leafgrapple.admin

### 注意事项
- 需要 Paper / Folia 1.21.4+
- ItemsAdder Modern 支持需要 ItemsAdder 4.0.13+
- 当前修改配置后需要重启服务器
- 当前仅支持抓取方块，当前暂不支持抓取实体，当前暂不支持拉动实体，钩头暂不会自动根据方块命中面旋转贴合
- 每个正在使用的钩爪会生成一个 ItemDisplay
- 不建议设置过大的钩爪飞行时间或最大距离
