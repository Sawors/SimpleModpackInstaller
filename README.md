# SimpleModpackInstaller #

This tool is used to simplify the installation process of Minecraft Modpacks published as Git repositories.

It can also be used to organise the use of multiple modpacks with the default Minecraft launcher by offering the option to create a custom profile per modpack. 


## Features Summary : ##
- [keep each Modpack in his own directory](#directory-per-modpack)
- [create a new profile in the Minecraft Laucher](#profile-creation)
- [automatically set the required amount of RAM](#auto-ram)
- [copy your settings to your new profile](#copying-infos)
- [copy your resourcepacks to your new profile](#copying-infos)
- [copy your shaderpacks to your new profile](#copying-infos)


## Appearance : ##

![Alt text](installer_appearance.png?raw=true "Appearance")

## How to Install a Modpack ##
1) put the modpack .git link in **Modpack :** (the one you get for instance from Github)  
`Code > HTTPS > https://github.com/Author/Modpack.git`  

2) in **Where to install ?** leave blank for auto profile creation (**highly recommended**) or input your own directory. The installer will **always** create a */MODPACK_NAME/* directory where you want to install.  

3) if you want to create a profile in the launcher tick **[ ] Create profile**  

4) if you choose to create a profile, specify which Minecraft data you want to copy  

5) by default, the JVM arguments are automatically set, however you can still edit them (**not recommended**) (before installation or directly in the Minecraft launcher)  

7) specify the desired Minecraft version in **Version** (menu right of the JVM arguments) (default : *latest_release*)  

8) click install


## Features : ##
### Directory per Modpack ###
Each time you install a Modpack using the SimpleModpackInstaller, 
a new directory is created in */.minecraft/profiles/* with the modpack name : */.minecraft/profiles/MODPACK_NAME/*.
If the directory already exists, a new one is created with MODPACK_NAME_1 or bigger number for each copy.
### Profile Creation ###
When the Modpack is downloaded and you ticked the **Create profile** box, a new profile is added to your Minecraft Launcher with the modpack directory set as it's *Game Directory*
### Auto RAM ###
The newly created profile is initialised with the highest ram value for your system
without going over 10 Go as Minecraft does not really benefits of RAM amount over 10/12 Go
| System RAM | Profile RAM |
|------------|-------------|
| ≤ 8 Go | 5 Go |
| ≤ 16 Go | 8 Go |
| > 16 Go | 10 Go |

### Copying infos ###
You can specify if you want to copy to your profile your Minecraft Settings, ResourcePacks and ShaderPacks.
If you copy your settings your optifine settings will also be copied. 
| Original Directory | New Directory |
|------------|-------------|
| .minecraft/options.json | .minecraft/profiles/MODPACK_NAME/options.json |
| .minecraft/optionsof.json | .minecraft/profiles/MODPACK_NAME/optionsof.json |
| .minecraft/resourcepacks/* | .minecraft/profiles/MODPACK_NAME/resourcepacks/* |
| .minecraft/shaderpacks/* | .minecraft/profiles/MODPACK_NAME/shaderpacks/* |
