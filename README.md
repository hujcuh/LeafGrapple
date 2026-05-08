## English Version

### plugin designed for Paper / Folia 1.21.4+.### Introduction

LeafGrapple is suitable for servers that need custom grapple items, modern item models, configurable movement parameters, improved grapple pulling physics, entity binding, durability, cooldowns, visible hook-chain effects, and ItemsAdder Modern graphics support.

The plugin supports both traditional block-based grappling and optional entity grappling. Server owners can configure each grapple type individually, including whether it can hook mobs or players, bind duration, pull-back radius, periodic damage, and visual effects.

### Features

- Virtual grappling hook flight system
- Individually configurable grapple types
- Supports multiple item recognition methods:
  - PDC
  - `item_model`
  - legacy `CustomModelData`
- Supports hiding technical item lore information
- Supports `/leafgrapple reload` / `/lg reload` for reloading configuration

### Commands

- `/leafgrapple give <type>` - Gives the specified grapple type
- `/leafgrapple list` - Lists all available grapple types
- `/leafgrapple reload` - Reloads the plugin configuration
- `/grapple` - Alias of `/leafgrapple`
- `/lg` - Alias of `/leafgrapple`

### Permissions

- `leafgrapple.admin`
  - Allows using administrator commands, such as giving grapples and reloading configuration.

- `leafgrapple.entityhook.bypass`
  - Players with this permission cannot be bound by entity grapple effects.

### Configuration

Each grapple type can be configured individually in `config.yml`.

Configurable options include:

- Grapple display name
- Held item material
- Held item model
- Flying hook head material
- Flying hook head model
- Maximum launch distance
- Maximum pull-back distance
- Launch speed
- Pull acceleration
- Maximum pull speed
- Cooldown time
- Durability settings
- Item lore display settings
- Block grapple display settings
- Entity grapple toggle
- Entity grapple target types
- Entity grapple bind parameters
- Entity grapple periodic damage
- Entity grapple particle chain
- Entity grapple anchor display item

### Entity Grapple

Entity grapple is an optional feature and can be enabled separately for each grapple type. When enabled, if the flying grapple hits a valid entity first, it will enter the entity binding flow instead of the block pull-back flow.

Entity binding is not a hard lock and does not directly teleport the target. The target can move within a configurable radius. If it moves beyond that radius, it will be gently pulled back, creating a restraint effect similar to being held by a chain or rope.

Entity grapple can be configured with:

- Whether entity grapple is enabled
- Whether players can be hooked
- Whether mobs can be hooked
- Whether world PVP must be enabled
- Bypass permission
- Bind duration
- Bind radius
- Pull-back strength
- Maximum pull-back speed
- Velocity damping
- Periodic damage
- Particle chain
- Anchor display item

### Notes

- Requires Paper / Folia 1.21.4+
- ItemsAdder Modern support requires ItemsAdder 4.0.13+
- Configuration can be reloaded with `/lg reload`
- Reloading configuration clears active block grapple and entity grapple sessions
- Reloading configuration does not clear player cooldowns by default
- Extremely large grapple flight time, maximum distance, maximum pull-back distance, or pull speed values are not recommended
- Extremely high entity grapple particle density is not recommended
- When upgrading from an older version, it is recommended to regenerate or manually merge `config.yml`



---

## 中文版

### 简介

LeafGrapple（叶子钩爪）是一个面向 Paper / Folia 1.21.4+ 的虚拟钩爪插件。

LeafGrapple 适合需要自定义钩爪物品、现代物品模型、可配置移动参数、改进型钩爪拉扯物理、实体束缚、耐久、冷却、可视化链路效果和 ItemsAdder Modern graphics 支持的服务器。

插件支持传统的方块钩爪玩法，也支持可选的实体钩爪玩法。服主可以为每种钩爪单独配置是否允许钩住生物或玩家，并设置束缚时间、回拉半径、周期伤害和视觉效果。

### 功能介绍

- 虚拟钩爪飞行系统
- 支持钩爪单独配置
- 支持 PDC、item_model 和旧 CustomModelData 多种识别方式
- 支持隐藏物品 Lore 中的技术信息
- 支持 `/leafgrapple reload` / `/lg reload` 重载配置

### 命令

- `/leafgrapple give <类型>` - 获取指定类型的钩爪
- `/leafgrapple list` - 查看当前可用钩爪类型
- `/leafgrapple reload` - 重载插件配置
- `/grapple` - `/leafgrapple` 的别名
- `/lg` - `/leafgrapple` 的别名

### 权限

- `leafgrapple.admin`
  - 允许使用管理员命令，例如获取钩爪和重载配置。

- `leafgrapple.entityhook.bypass`
  - 拥有该权限的玩家不会被实体钩爪束缚。

### 配置说明

每种钩爪都可以在 `config.yml` 中单独配置。

支持配置内容包括：

- 钩爪显示名称
- 手持物品材质
- 手持物品模型
- 飞行钩头材质
- 飞行钩头模型
- 最大发射距离
- 最大拉回距离
- 发射速度
- 拉扯加速度
- 最大拉扯速度
- 冷却时间
- 耐久设置
- 物品 Lore 显示设置
- 方块钩爪显示参数
- 实体钩爪开关
- 实体钩爪目标类型
- 实体钩爪束缚参数
- 实体钩爪周期伤害
- 实体钩爪粒子链路
- 实体钩爪锚点显示物

### 实体钩爪说明

实体钩爪是一个可选功能，可以为每种钩爪单独启用。启用后，钩爪飞行过程中如果先命中可用实体，则会进入实体束缚流程，而不是方块拉回流程。

实体束缚不是硬锁定，也不会直接传送目标。目标可以在一定半径内移动，超出半径后会被柔和拉回，表现更接近被锁链或绳索牵制。

实体钩爪可以配置：

- 是否启用
- 是否允许钩玩家
- 是否允许钩生物
- 是否要求世界 PVP 开启
- 绕过权限
- 束缚持续时间
- 束缚半径
- 回拉强度
- 最大回拉速度
- 速度阻尼
- 周期伤害
- 粒子链路
- 锚点显示物

### 注意事项

- 需要 Paper / Folia 1.21.4+
- ItemsAdder Modern 支持需要 ItemsAdder 4.0.13+
- 修改配置后可以使用 `/lg reload` 重载配置
- 重载配置时会清理当前正在使用的方块钩爪和实体钩爪会话，重载配置默认不会清除玩家冷却
- 不建议设置过大的钩爪飞行时间、最大距离、最大拉回距离或拉扯速度，不建议设置过高的实体钩爪粒子密度
- 从旧版本升级建议重新生成或手动合并 config.yml
