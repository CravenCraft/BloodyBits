## V1.1.0
- Added a config setting to still show blood explosions on an entity's death even if blood chunks are set to **false**.
- **Blood chunks** & **show entity damage** are now **false** by default since they are still experimental, and not as polished as
    I would like.
- Fixed the issue with the blood spatters flashing sometimes when landing.
- Fixed the issue with blood spatters glowing slightly in the dark.
- Mad blood spatters slightly more transparent to make them feel just a little more liquid-like.

## V1.0.1
- Removed logger from blood spray event.
- Switched to LivingDamageEvent to prevent undamaged entities such as creative playersfrom producing blood.

## V1.0.0
The initial creation of the mod's framework. 