# SimpleModpackInstaller

This tool is used to simplify the installation process of Minecraft Modpacks published as a Git repository.

It can also be used to organise the use of multiple modpacks with the default Minecraft launcher by offering the option of creating a custom profile per modpack. 


## Features Summary:
- keep each Modpack in his own directory
- create a new profile in the Minecraft Laucher
- automatically set the required amount of RAM
- copy your settings to your new profile
- copy your resourcepacks to your new profile
- copy your shaderpacks to your new profile


## Appearance :

![Alt text](installer_appearance.png?raw=true "Appearance")


## Features :
### Directory per Modpack
Each time you install a Modpack using the SimpleModpackInstaller, 
a new directory is created in */.minecraft/profiles/* with the modpack name : */.minecraft/profiles/MODPACKNAME/*.
If the directory already exists, a new one is created with MODPACKNAME_1 or bigger number for each copy.
### Profile Creation
When the Modpack is downloaded and you ticked the **Create profile** box, a new profile is added to your Minecraft Launcher with the modpack directory set as it's *Game Directory*
### Auto RAM
The newly created profile is initialised with the highest ram value for your system :
| System RAM | Profile RAM |
| <= 8Go | 5Go |
| <= 16Go | 8Go |
| > 16Go | 10Go |

### Copying infos
