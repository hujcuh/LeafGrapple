**LeafGrapple** is a configurable virtual grappling hook plugin for **Paper / Folia 1.21.4+**.

It supports block grappling, optional entity grappling, custom grapple types, durability, cooldowns, modern item models, legacy CustomModelData, and visible hook-chain effects.

## Features

- Virtual grappling hook flight system
- Right-click to launch a grapple
- Right-click again to pull yourself after the hook attaches to a block
- Left-click to cancel the current grapple
- Main-hand and off-hand support
- Configurable grapple types
- Configurable launch distance, pull distance, speed, acceleration, cooldown, and durability
- Improved pull movement with arc-like motion, upward assistance, edge step assistance, and finish hop
- Optional entity grappling system
- Periodic damage for hooked entities
- Visible particle chain and hook anchor display for entity hooks
- Supports modern `item_model`,legacy `CustomModelData`,PDC-based item recognition
- Supports ItemsAdder Modern graphics model paths

## Commands

- `/leafgrapple give <type>` - Give yourself a grapple
- `/leafgrapple list` - List available grapple types
- `/leafgrapple reload` - Reload the plugin configuration
- `/grapple` - Alias of `/leafgrapple`
- `/lg` - Alias of `/leafgrapple`

## Permissions

- `leafgrapple.admin`
  - Allows access to admin commands such as giving grapples and reloading the configuration.

- `leafgrapple.entityhook.bypass`
  - Prevents the player from being bound by entity grapple effects.

## Notes

- Requires **Paper / Folia 1.21.4+**
- ItemsAdder Modern support requires **ItemsAdder 4.0.13+**
- Entity grappling can be enabled or disabled per grapple type
- Player hooking is configurable and can be disabled for safer gameplay
- Entity hook damage uses the normal server damage system instead of true damage
- Configuration can be reloaded with `/lg reload`

### Resource Pack / ItemsAdder Models

LeafGrapple supports two model methods. Choose **one** method for each grapple type.

#### CustomModelData Resource Pack(Demo-CMD pack)

Use this if you are using a normal Minecraft resource pack.
```yaml
hooks:
  wood:
    item: PAPER:50000
    display-item: PAPER:50001
```
#### ItemsAdder Modern Graphics

Use this if your server uses ItemsAdder.
```yaml
hooks:
  wood:
    item-material: PAPER
    item-model: "leafgrapple:wood_grapple"
    display-item-material: PAPER
    display-item-model: "leafgrapple:wood_grapple_head"
   ```
   
---

## 中文版

**LeafGrapple（叶子钩爪）** 是一个面向 **Paper / Folia 1.21.4+** 的可配置虚拟钩爪插件。

它支持方块钩爪、可选的实体钩爪、自定义钩爪类型、耐久、冷却、现代物品模型、旧版 CustomModelData，以及可视化的钩爪链路效果。

## 功能

- 虚拟钩爪飞行系统
- 右键发射钩爪
- 钩爪命中方块后，再次右键将自己拉向钩点
- 左键取消当前钩爪
- 支持主手和副手使用
- 支持自定义钩爪类型
- 可配置发射距离、拉回距离、速度、加速度、冷却和耐久
- 改进型拉扯移动，支持轻微弧线、上升辅助、边缘越障辅助和终点上弹
- 可选的实体钩爪系统
- 支持对被钩住的实体造成周期伤害
- 实体钩爪支持可视化粒子链路和钩点显示物
- 支持现代 `item_model`、旧版 `CustomModelData` 和 PDC 物品识别
- 支持 ItemsAdder Modern graphics 模型路径

## 命令

- `/leafgrapple give <类型>` - 获取指定类型的钩爪
- `/leafgrapple list` - 查看可用钩爪类型
- `/leafgrapple reload` - 重载插件配置
- `/grapple` - `/leafgrapple` 的别名
- `/lg` - `/leafgrapple` 的别名

## 权限

- `leafgrapple.admin`
  - 允许使用管理员命令，例如获取钩爪和重载配置。

- `leafgrapple.entityhook.bypass`
  - 拥有该权限的玩家不会被实体钩爪束缚。

## 注意事项

- 需要 **Paper / Folia 1.21.4+**
- ItemsAdder Modern 支持需要 **ItemsAdder 4.0.13+**
- 实体钩爪可以为每种钩爪单独开启或关闭
- 是否允许钩住玩家可以单独配置，适合需要更安全玩法的服务器
- 实体钩爪伤害不是“真伤”，而是走服务器正常伤害流程
- 可以使用 `/lg reload` 重载配置
