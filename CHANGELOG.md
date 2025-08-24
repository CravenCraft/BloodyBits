## V1.3.3 Texture Polishing & Bugfixes
- Switched the blood spray texture back to its original, more solid texture. Think it looks a lot better.
- TODO: Added a client-side config option to make the blood spatters more bright, which should fix a minor
        issue that shows them as much darker when on ceilings regardless of lighting. As a consequence, this may
        make spatters in general brighter in caves and such. Still working on a fix that resolves this without
        compromising the overall look.
- Modified the blood spatter textures slightly.
- Fixed a bug causing the game to crash when entities bleed due to a divide by zero error.
  Special thanks to [encode42](https://github.com/encode42) for this fix.
- Fixed an issue where blood spatters would reset back to red whenever the player would leave then return to the chunk.

## V1.3.2 More Bug Fixes
- Fixed a bug causing explosive events to spray blood from gun sources such as the player.
    Special thanks to [slava110](https://github.com/slava110) for this fix.
- Made the **show_mob_damage** client config always set to false until it is fully ready to be implemented. Should 
    prevent any accidental server crashes if someone configures it.
- Fixed a bug that was causing server crashes (Forgot I didn't move the client events to their own client-side only class). 
- Updated README to reflect recent minor version bump.

## V1.3.1
- Hotfix for a potential game crashing bug with certain entities.

## V1.3.0
**NOTE: _HIGHLY_ RECOMMENDED TO DELETE PREVIOUS CONFIG FILES PRIOR TO UPDATING.**
- Remade all blood spatter textures to be a bit more detailed with different opacities.
- Exploding mobs such as creepers now produce huge blood sprays on explosion.
- Fixed bug that allows blood spatters to catch on fire.
- Fixed bug that causes a blood fountain whenever the player dies, but does not respawn.
- Added an option to blacklist the player (all players) from being an entity that can bleed.
- Stopped blood from triggering buttons and interacting with other entities.
- Removing blood sprays/chunks on entity death until I find a better & less crash-prone solution.
- Major rework to blood entities. They no longer extend the AbstractArrow class, which fixes the catching fire, button pressing, 
    and some crashing bugs. As well, allowed me to remove some mixin classes that were prone to errors when using Sinytra Connector.
- Added config option to modify the blood spray radius.
- Fixed minor bug that caused blood to not spray in certain directions.
- Added an optional bleed effect (default is false. Enable in config.) when an entity is below 50% health that bleeds more as they get lower in health.
- Experimenting with a system for showing entity damage. If it viable, it will be fully implemented in the future.

## V1.2.1
- Added a quick fix for a potential crash whenever the player takes damage.
- Set the default blood spatter amount to 500 and blood chunk amount to 250.
- Added & reordered entity null checks for blood bits.
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