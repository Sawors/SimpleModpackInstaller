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
a new directory is created in */.minecraft/profiles/* with the modpack name : */.minecraft/profiles/MODPACK_NAME/*.
If the directory already exists, a new one is created with MODPACK_NAME_1 or bigger number for each copy.
### Profile Creation
When the Modpack is downloaded and you ticked the **Create profile** box, a new profile is added to your Minecraft Launcher with the modpack directory set as it's *Game Directory*
### Auto RAM
The newly created profile is initialised with the highest ram value for your system
without going over 10 Go as Minecraft does not really benefits of RAM amount over 10/12 Go
| System RAM | Profile RAM |
|------------|-------------|
| ≤ 8 Go | 5 Go |
| ≤ 16 Go | 8 Go |
| > 16 Go | 10 Go |

### Copying infos
You can specify if you want to copy to your profile your Minecraft Settings, ResourcePacks and ShaderPacks.
If you copy your settings your optifine settings will also be copied. 
| Original Directory | New Directory |
|------------|-------------|
| .minecraft/options.json | .minecraft/profiles/MODPACK_NAME/options.json |
| .minecraft/optionsof.json | .minecraft/profiles/MODPACK_NAME/optionsof.json |
| .minecraft/resourcepacks/* | .minecraft/profiles/MODPACK_NAME/resourcepacks/* |
| .minecraft/shaderpacks/* | .minecraft/profiles/MODPACK_NAME/shaderpacks/* |
