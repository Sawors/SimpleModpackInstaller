package com.github.sawors.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.time.ZoneId;
import java.util.*;


public class ModpackInstallControler {
    
    public TextField urlfield;
    public Button install;
    public TextField filefield;
    public Button browsebutton;
    public Label urlerrormessage;
    public Label fileerrormessage;
    public Text defaultfoldermessage;
    public CheckBox profilecheck;
    public Label installerrormessage;
    public AnchorPane datainput_LAYER;
    public AnchorPane installing_LAYER;
    public ProgressBar progressbar;
    public TextArea gitoutputmessage;
    public ChoiceBox<String> versionselect;
    public TextField jvmargsfield;
    public Text progresspercenttext;
    public Pane copyinfosbox;
    public CheckBox copymcsettings;
    public CheckBox copymcrp;
    public CheckBox copymcshaders;
    public Text installingshowtext;
    public Button closebutton;
    public Text versiondisplay;
    public Button openfolder;
    
    
    private StringBuilder profilename = new StringBuilder();
    ArrayList<AnchorPane> layerlist = new ArrayList<>();
    private double progress = 0;
    private final ObservableList<String> versionlist = FXCollections.observableList(new ArrayList<>());
    private final File mc_root = new File(System.getenv("APPDATA")+"\\.minecraft");
    private final File profilesfile = new File(mc_root+"\\launcher_profiles.json");
    private File installdir;
    private String image = "";
    private final long ramamount = Runtime.getRuntime().totalMemory();
    Paint errorpaint = Paint.valueOf("#B22222");
    Paint successpaint = Paint.valueOf("#008000");
    Paint warningpaint = Paint.valueOf("#ffa500");
    
    
    
    
    public void initialize() {
        
        versiondisplay.setText("version : release  "+InstallerApp.getVersion());
        
        try{
            image = new String(Base64.getEncoder().encode(Objects.requireNonNull(InstallerApp.class.getResourceAsStream("resource_img.png")).readAllBytes()));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    
        layerlist.add(datainput_LAYER);
        layerlist.add(installing_LAYER);
    
        setLayer(datainput_LAYER);
        
        
        
        File[] versions = new File(mc_root + "\\versions\\").listFiles();
        versionlist.add("latest-release");
        if(versions != null){
            for(File v : versions){
                StringBuilder foo = new StringBuilder(v.toString());
                foo.replace(0, foo.lastIndexOf("\\versions\\")+10, "");
                versionlist.add(foo.toString());
            }
        }
        
        versionselect.setValue(versionlist.get(0));
        
        if(!mc_root.exists()){
            installerrormessage.setText("Minecraft root directory (.minecraft) cannot be found on your system");
            install.setDisable(true);
        } else if(!profilesfile.exists()){
            installerrormessage.setText("Minecraft profiles file (launcher_profiles.json) cannot be found on your system");
            profilecheck.setDisable(true);
        }
        
        
        versionselect.setItems(versionlist);
        
        
        displayDefaultMessage();
        
        
        String ram;
        if(ramamount <= 8 && ramamount > 5){
            ram = "5";
        } else if(ramamount <= 16){
            ram = String.valueOf((int)(0.8*ramamount));
        } else {
            ram = "12";
        }
        
        String jvm = "-Xmx"+ram+"G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
        jvmargsfield.setText(jvm);
        
        //PROFILE SETUP
        
    
    }
    
    //
    // MAIN METHOD, INSTALL THE MODPACK
    //
    public void installModpack(ActionEvent actionEvent) {
        
        //check for valid URL input
        if(!checkModpackURL(urlfield, urlerrormessage, false)){
            showErrorText(installerrormessage, "installation failed, please check your inputs (URL)");
            return;
        }
    
        //data setup
        File packlocation;
        String repourl = urlfield.getText();
        
        if(filefield.getText().length() <= 0){
            profilename= new StringBuilder();
            for(int i = urlfield.getText().length()-1; i >= 0; i--){
                if(urlfield.getText().toCharArray()[i] == '/' || urlfield.getText().toCharArray()[i] == '\\') break;
                profilename.append(urlfield.getText().toCharArray()[i]);
            }
            profilename.reverse();
            profilename = new StringBuilder(profilename.toString().replaceAll(".git", ""));
    
            packlocation = new File(mc_root+File.separator+"profiles"+File.separatorChar+profilename.toString());
        } else {
            packlocation = new File(filefield.getText()+File.separator+profilename.toString());
        }
        
        //check if modpack with the same name already exist
        //TODO add update dialog here -> "update pack or create a copy ?"
        
        if(packlocation.exists()){
            File baselocation = packlocation;
            String basename = profilename.toString();
            for (int num = 1; packlocation.exists(); num++) {
                    packlocation = new File(baselocation+" (" + num+")");
                    profilename = new StringBuilder(basename + " (" + num+")");
                    System.out.println(num);
            }
            System.out.println(profilename);
        }
        installdir = packlocation;
        boolean createlaucherprofile = profilecheck.isSelected();
        
        // EVERY CHECK ARE MADE, DATA ARE ASSIGNED, BEGINNING OF THE INSTALLATION PROCESS
    
        installdir.mkdirs();
    
        progresspercenttext.setText("0%");
        closebutton.setVisible(true);
        closebutton.setDisable(true);
        openfolder.setDisable(true);
        openfolder.setVisible(true);
        setLayer(installing_LAYER);
    
        //https://github.com/Sawors/Stones_ResourcePack.git
        //
    
        TextProgressMonitor monitor = new TextProgressMonitor(){
        
            // total phases : 12+1
            // git clone takes 90% of the process
            int phase = 0;
        
            @Override
            public void beginTask(String title, int work) {
                super.beginTask(title, work);
                phase++;
                System.out.println(phase);
            }
        
            @Override
            protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
                super.onUpdate(taskName, cmp, totalWork, pcnt);
                System.out.println(phase);
                System.out.println(progress);
                Platform.runLater(() -> gitoutputmessage.appendText("\n "+taskName + " : "+ pcnt + "%  ("+cmp+"/"+totalWork+")"));
            
                progress = ((0.9f*phase)/13)+((0.9*pcnt)/1300);
                Platform.runLater(() -> progressbar.setProgress(progress));
                Platform.runLater(() -> progresspercenttext.setText(((int)(progress*100))+"%"));
            
                if(pcnt == 100){
                    Platform.runLater(() -> gitoutputmessage.appendText("\n  >  "+taskName+" : done !"+"\n"));
                    phase++;
                }
            
            }
    
            @Override
            protected void onEndTask(String taskName, int workCurr) {
                super.onEndTask(taskName, workCurr);
            }
        };
    
        CloneCommand command = Git.cloneRepository().setURI(repourl).setDirectory(installdir);
        command.setProgressMonitor(monitor);
        monitor.start(1);
    
        //delete .git directory
        //
        Thread workthread = new Thread(() -> {
            Git result = null;
            try {
                result = command.call();
            } catch (GitAPIException e) {
                Platform.runLater(() -> gitoutputmessage.appendText("\n  >  Downloading Modpack : GIT ERROR\n\n"));
                Platform.runLater(() -> progressbar.setVisible(false));
                Platform.runLater(() -> progresspercenttext.setVisible(false));
                Platform.runLater(() -> installingshowtext.setFill(errorpaint));
                Platform.runLater(() -> installingshowtext.setText("Installation failed"));
                Platform.runLater(() -> closebutton.setDisable(false));
                installdir.delete();
                e.printStackTrace();
                return;
            } catch (JGitInternalException e2){
                Platform.runLater(() -> gitoutputmessage.appendText("\n  >  Downloading Modpack : REPOSITORY NOT FOUND\n\n"));
                Platform.runLater(() -> progressbar.setVisible(false));
                Platform.runLater(() -> progresspercenttext.setVisible(false));
                Platform.runLater(() -> installingshowtext.setFill(errorpaint));
                Platform.runLater(() -> installingshowtext.setText("Installation failed"));
                Platform.runLater(() -> closebutton.setDisable(false));
                installdir.delete();
                e2.printStackTrace();
                return;
            }
            
            Platform.runLater(() -> gitoutputmessage.appendText("\n  >  Downloading Modpack : DONE !\n\n"));
            if(createlaucherprofile){
                writeProfileToFile(installdir);
            }
            copyMCInfos(installdir);
            monitor.endTask();
            if (result != null) {
                result.close();
            }
    
            //delete .git directory
            File deletegit = new File(installdir + File.separator + ".git");
            try {
                if (deletegit.exists()) {
                    try {
                        FileUtils.deleteDirectory(deletegit);
                    } finally {
                        if (deletegit.exists()) {
                            deletegit.delete();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //
            progresspercenttext.setVisible(false);
            progressbar.setVisible(false);
            installingshowtext.setText("Successfully Installed");
            closebutton.setDisable(false);
            closebutton.setVisible(true);
            openfolder.setDisable(false);
            openfolder.setVisible(true);
        });
        
            workthread.start();
        
    }
    
    public void openBrowser(ActionEvent actionEvent) {
        DirectoryChooser directorychooser = new DirectoryChooser();
        File minecraftdirectory = mc_root;
        if(minecraftdirectory.exists()) directorychooser.setInitialDirectory(minecraftdirectory);
        
        File directory = directorychooser.showDialog(browsebutton.getScene().getWindow());
        if(directory != null){
            filefield.setText(directory.toString());
            checkInstallLocation(directory.toString(), fileerrormessage);
        }
    }
    
    private void displayDefaultMessage(){
        if(new File(mc_root+"\\profiles\\").exists()){
            defaultfoldermessage.setText("default location : "+ new File(mc_root +"\\profiles\\"+profilename.toString()));
        } else {
            new File(mc_root+"\\profiles\\").mkdir();
        }
    }
    
    public void openFolder(ActionEvent actionEvent) {
        try {
            Runtime.getRuntime().exec("explorer.exe  /select," + installdir.getAbsolutePath()+File.separatorChar+"mods");
        } catch (IOException ignored) {
        }
    }
    
    private void setLayer(AnchorPane layer){
        for(AnchorPane pane : layerlist){
            if(pane == layer){
                pane.setVisible(true);
                pane.setDisable(false);
                pane.requestFocus();
            } else {
                pane.setVisible(false);
                pane.setDisable(true);
            }
        }
    }
    
    public void setJVMArgs(ActionEvent actionEvent) {
    }
    
    /*
    copy .minecraft datas to profile directory :
        - options.txt
        - optionsof.txt
        - resourcepacks/
        - shaderpacks/
     */
    public void copyMCInfos(File modpackdir){
        File profiledirectory = new File(modpackdir+File.separator);
        File settings = new File(mc_root+File.separator+"options.txt");
        File settingsoptifine = new File(mc_root+File.separator+"optionsof.txt");
        File resourcepacks = new File(mc_root+File.separator+"resourcepacks");
        File shaderpacks = new File(mc_root+File.separator+"shaderpacks");
        
        //COPY SETTINGS + OPTIFINE SETTINGS
        if(copymcsettings.isSelected()){
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying settings : copying..."));
            try{
                if(settings.exists() && profiledirectory.exists()){
                    org.apache.commons.io.FileUtils.copyFileToDirectory(settings, profiledirectory);
                }
                if(settingsoptifine.exists() && profiledirectory.exists()){
                    org.apache.commons.io.FileUtils.copyFileToDirectory(settingsoptifine, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying settings : done !"));
        }
        progress += 0.05/3f;
        Platform.runLater(() -> progressbar.setProgress(progress));
        Platform.runLater(() -> progresspercenttext.setText(((int)(progress*100))+"%"));
        
        //COPY RESOURCE PACKS
        if(copymcrp.isSelected()){
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying resource packs : copying..."));
            try{
                if(resourcepacks.exists() && profiledirectory.exists()){
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(resourcepacks, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying resource packs : done !"));
        }
        progress += 0.05/3f;
        Platform.runLater(() -> progressbar.setProgress(progress));
        Platform.runLater(() -> progresspercenttext.setText(((int)(progress*100))+"%"));
        
        // COPY SHADERPACKS
        if(copymcshaders.isSelected()){
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying shader packs : copying..."));
            try{
                if(shaderpacks.exists() && profiledirectory.exists()){
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(shaderpacks, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            Platform.runLater(() -> gitoutputmessage.appendText("\ncopying shader packs : done !"));
        }
        
    }
    
    /*
    writing the profile.json file
     */
    public void writeProfileToFile(File modpackdir){
    
        try{
            Platform.runLater(() -> gitoutputmessage.appendText("\ncreating profile : creating"));
            Gson gson = new Gson();
            JsonObject jsonobj = gson.fromJson(new FileReader(profilesfile), JsonObject.class);
            File file = new File(mc_root+File.separator+"launcher_profiles_SMIbackup.json");
            file.createNewFile();
        
            byte[] foo;
            try(FileInputStream in = new FileInputStream(profilesfile)){
                foo = in.readAllBytes();
            }
            try(FileOutputStream out = new FileOutputStream(file)){
                out.write(foo);
            }
        
        
            StringBuilder time = new StringBuilder(Clock.systemUTC().instant().atZone(ZoneId.systemDefault()).toString());
            time.delete(time.length()-26, time.length());
            time.append('Z');
        
        
            HashMap<String, String> map = new HashMap<>();
            map.put("created", time.toString());
            map.put("gameDir", installdir.getAbsolutePath());
            map.put("icon", "data:image/png;base64,"+ image);
            map.put("javaArgs", jvmargsfield.getText());
            map.put("lastUsed", time.toString());
            map.put("lastVersionId", versionselect.getValue());
            map.put("name", profilename.toString());
            map.put("type", "custom");
        
        
            jsonobj.get("profiles").getAsJsonObject().add(String.valueOf(UUID.randomUUID()), JsonParser.parseString(gson.toJson(map)));
        
        
            try(FileOutputStream outs = new FileOutputStream(profilesfile)){
                outs.write(new GsonBuilder().setPrettyPrinting().create().toJson(jsonobj).getBytes());
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Platform.runLater(() -> gitoutputmessage.appendText("\ncreating profile : done !"));
        }
        progress += 0.05;
        Platform.runLater(() -> progressbar.setProgress(progress));
        Platform.runLater(() -> progresspercenttext.setText(((int)(progress*100))+"%"));
    }
    
    public void closeButtonPressed(ActionEvent actionEvent) {
        Platform.exit();
    }
    
    public String getModpackName(String url){
        StringBuilder output = new StringBuilder();
        String input = url.replaceAll(".git", "");
        for(int i = input.length()-1; i >= 0; i--){
            if(input.toCharArray()[i] == '/' || input.toCharArray()[i] == '\\') break;
            output.append(input.toCharArray()[i]);
        }
        return output.toString();
    }
    
    public void showErrorText(Labeled field, String text){
        field.setTextFill(errorpaint);
        field.setText(text);
        Timer timer1 = new Timer("error message vanish");
        try{
            timer1.schedule(new TimerTask()
            {
                @Override
                public void run(){
                    field.setText(" ");
                }
            }, 5000L);
        } finally {
            timer1.cancel();
        }
    }
    
    //
    //      CHECKS FOR USER INPUT FIELDS
    //
    
    public boolean checkModpackURL(TextInputControl urlfield, Labeled errortext, boolean allowempty){
        boolean showerror = errortext != null;
        String url = urlfield.getText();
        url = url.replaceAll(" ","");
        StringBuilder checkgit = new StringBuilder();
        if(!allowempty && urlfield.getText().length() <= 4 && showerror){
            showErrorText(errortext, "Please specify a Git repository");
            return false;
        }
    
        //checks for valid url
        try{
            //I create a URL without assigning it to a variable only to check its validity
            new URL(urlfield.getText());
        } catch (MalformedURLException e) {
            if(showerror){
                showErrorText(errortext, "You must input a valid URL");
            }
            return false;
        }
    
        // checks for .git at the end of the URL
        for(int i = url.length()-4; i <= url.length()-1; i++){
            checkgit.append(url.toCharArray()[i]);
        }
        if(showerror && !checkgit.toString().equalsIgnoreCase(".git")){
            showErrorText(errortext, "You must input a valid Git repository (.git)");
            return false;
        }
        if(checkgit.toString().equalsIgnoreCase(".git")){
            if(showerror && !allowempty){
                errortext.setTextFill(successpaint);
                errortext.setText("✓");
            }
            return true;
        }
        return false;
    }
    
    
    public boolean checkModpackURL(TextInputControl urlfield, boolean allowempty){
        return checkModpackURL(urlfield, null, allowempty);
    }
    
    
    public boolean checkInstallLocation(String location, Labeled erroroutput){
        boolean showoutput = erroroutput != null;
        if(location == null || location.length()<=0){
            return true;
        }
        
        File input = new File(location);
        
        if(input.exists()){
            if(input.isDirectory()){
                //check if input leads to a not empty folder and puts a warning in case it is
                System.out.println(input.listFiles().length);
                System.out.println(showoutput);
                if(Objects.requireNonNull(input.listFiles()).length > 32 && showoutput){
                    System.out.println("bigboi");
                    erroroutput.setTextFill(warningpaint);
                    erroroutput.setText("Your destination is a big directory, be careful !");
                    return true;
                }
            }else {
                if(showoutput){
                    erroroutput.setTextFill(errorpaint);
                    erroroutput.setText("Your destination is not a directory");
                }
                return false;
            }
        }
        
        if(showoutput){
            erroroutput.setTextFill(successpaint);
            erroroutput.setText("✓");
        }
        return true;
    }
    
    private boolean checkAllFields(boolean allowempty){
        return checkModpackURL(urlfield, urlerrormessage, allowempty) && checkInstallLocation(filefield.getText(), fileerrormessage);
    }
    
    //
    // Methods triggered by events which trigger checks
    //
    @FXML
    private void urlInputCheck(ActionEvent event){
        checkModpackURL(urlfield, urlerrormessage, false);
    }
    @FXML
    private void fileInputCheck(ActionEvent event){
        checkInstallLocation(filefield.getText(), fileerrormessage);
    }
    @FXML
    private void globalCheckAllowEmpty(MouseEvent event){
        checkAllFields(true);
    }
    
    
}
