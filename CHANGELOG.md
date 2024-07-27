## V1.2.1
- Added a quick fix for a potential crash whenever the player takes damage.
- Set the default blood spatter amount to 500 and blood chunk amount to 250.
- Fixed a bug causing blood bits to not despawn in water sometimes.

## V1.2.0
- Added a config option to set the volume for both the blood spatters and explosions.
- Added a blacklist for damage sources. By default, I've included all vanilla sources that wouldn't make sense.
- Removed the common config check for **player radius**. This should automatically be done by Minecraft.

## V1.1.1
- Added a blacklist option to the config for any entities that may cause potential crashes with this mod. One known
    entity so far is **alexsmobs:cachalot_whale**.
- Updated the sounds for the blood spatters to be less slime-like.
- Added a few checks to ensure that any blood spray entities are removed if left hanging in the air too long.

## V1.1.0
- Added a config setting to still show blood explosions on an entity's death even if blood chunks are set to **false**.
- **Blood chunks** & **show entity damage** are now **false** by default since they are still experimental, and not as polished as
    I would like.
- Fixed the issue with the blood spatters flashing sometimes when landing.
- Fixed the issue with blood spatters glowing slightly in the dark.
- Mad blood spatters slightly more transparent to make them feel just a little more liquid-like.

## V1.0.1
- Removed logger from blood spray event.
- Switched to LivingDamageEvent to prevent undamaged entities such as creative players from producing blood.

## V1.0.0
The initial creation of the mod's framework.