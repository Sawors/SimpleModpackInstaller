package com.github.sawors.demo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import org.eclipse.jgit.lib.BatchingProgressMonitor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.UnsupportedAddressTypeException;
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
    public Text installerrormessage;
    public AnchorPane datainput_LAYER;
    public AnchorPane installing_LAYER;
    public ProgressBar progressbar;
    public TextArea gitoutputmessage;
    public Button cancelldownload;
    public ChoiceBox<String> versionselect;
    public TextField jvmargsfield;
    public Text progresspercenttext;
    public Pane copyinfosbox;
    public CheckBox copymcsettings;
    public CheckBox copymcrp;
    public CheckBox copymcshaders;
    public Text installingshowtext;
    public Button closebutton;
    
    private boolean createprofile = true;
    private boolean copyinfos = false;
    private StringBuilder profilename = new StringBuilder();
    ArrayList<AnchorPane> layerlist = new ArrayList<>();
    private double progress = 0;
    private String lastprogressstep = "no";
    private ObservableList<String> versionlist = FXCollections.observableList(new ArrayList<>());
    private static HashMap<UserData, String> userdata = new HashMap<>();
    private final File mc_root = new File(System.getenv("APPDATA")+"\\.minecraft");
    private File profilesfile = new File(mc_root+"\\launcher_profiles.json");
    private String image = "";
    private Thread workthread;
    private Thread mainthread = Thread.currentThread();
    private boolean cancelled = false;
    
    
    
    
    public void initialize() {
        
        
        
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
            createprofile = false;
        }
        
        
        versionselect.setItems(versionlist);
        
        
        displayDefaultMessage();
        
        
        String ram;
        if(Runtime.getRuntime().totalMemory() <= 8){
            ram = "5";
        } else if(Runtime.getRuntime().totalMemory() <= 16){
            ram = "8";
        } else {
            ram = "10";
        }
        
        String jvm = "-Xmx"+ram+"G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
        userdata.put(UserData.JVM, jvm);
        jvmargsfield.setText(jvm);
        
        //PROFILE SETUP
        
    
    }
    
    
    public void installModpack(ActionEvent actionEvent) {
        installerrormessage.setText("");
        
        try{
            URL url = new URL(urlfield.getText());
            StringBuilder checkgit = new StringBuilder();
    
            for(int i = url.toString().length()-4; i <= url.toString().length()-1; i++){
                checkgit.append(url.toString().toCharArray()[i]);
            }
            if(!checkgit.toString().equals(".git")){
                throw new MalformedURLException();
            }
        } catch (MalformedURLException e) {
            installerrormessage.setText("You must specify a valid Git repository. ");
            e.printStackTrace();
            return;
        }
    
        if(urlfield.getText().length() <= 0){
            installerrormessage.setText(installerrormessage+"\nYou must specify a Git repository. ");
            Timer timer1 = new Timer("message1");
            try{
                timer1.schedule(new TimerTask()
                {
                    @Override
                    public void run(){
                        installerrormessage.setText(" ");
                    }
                }, 5000L);
            } finally {
                timer1.cancel();
            }
            return;
        }
        
        userdata.put(UserData.MODPACK_URL, urlfield.getText());
        
        if(createprofile){
            userdata.put(UserData.FILE_PATH, userdata.get(UserData.FILE_PATH)+profilename);
        }
        if(filefield.getText().length() <= 0){
            if(createprofile){
                profilename= new StringBuilder();
                for(int i = urlfield.getText().length()-1; i >= 0; i--){
                    if(urlfield.getText().toCharArray()[i] == '/' || urlfield.getText().toCharArray()[i] == '\\') break;
                    profilename.append(urlfield.getText().toCharArray()[i]);
                }
                profilename.reverse();
                profilename = new StringBuilder(profilename.toString().replaceAll(".git", ""));
                
                userdata.put(UserData.FILE_PATH, mc_root + "\\profiles\\"+profilename.toString());
            } else {
                userdata.put(UserData.FILE_PATH, mc_root + "\\profiles\\");
            }
            
        } else {
            if(createprofile){
                userdata.put(UserData.FILE_PATH, filefield.getText()+"\\"+profilename.toString());
            }
        }
    
        userdata.put(UserData.VERSION, versionselect.getValue());
        
        
        // EVERY CHECKS ARE MADE, START OF THE INSTALLATION PROCESS
        
        
    
        progresspercenttext.setText("0%");
        closebutton.setVisible(true);
        closebutton.setDisable(true);
        
        try{
            setLayer(installing_LAYER);
        } finally {
            try{
                File file = new File(userdata.get(UserData.FILE_PATH));
                String basename = profilename.toString();
                if(file.exists()){
                    for (int num = 1; file.exists(); num++) {
                        file = new File(userdata.get(UserData.FILE_PATH)+"_" + num);
                        profilename = new StringBuilder(basename + "_" + num);
                    }
                }
                file.mkdirs();
                userdata.put(UserData.FILE_PATH, file.toString());
    
                
        
            } finally {
                //https://github.com/Sawors/Stones_ResourcePack.git
                //
                
                BatchingProgressMonitor monitor = new BatchingProgressMonitor(){
    
                    @Override
                    protected void onUpdate(String s, int i) {
                        gitoutputmessage.setText("downloading files : " + s + " : " + i+"\n");
                        if(!Objects.equals(s, lastprogressstep)){
                            progress += (4/30f);
                            progressbar.setProgress(progress);
                            progresspercenttext.setText(((int)(progress*100))+"%");
                        }
                        lastprogressstep = s;
                    }
    
                    @Override
                    protected void onEndTask(String s, int i) {
        
                    }
    
                    @Override
                    protected void onUpdate(String s, int i, int i1, int i2) {
    
                        gitoutputmessage.setText("downloading files : " + s +"\n");
                        if(!Objects.equals(s, lastprogressstep)){
                            progress += (4/30f);
                            progressbar.setProgress(progress);
                            progresspercenttext.setText(((int)(progress*100))+"%");
                        }
                        lastprogressstep = s;
                    }
    
                    @Override
                    protected void onEndTask(String s, int i, int i1, int i2) {
                    
                    }
                };
                
                CloneCommand command = Git.cloneRepository().setURI(userdata.get(UserData.MODPACK_URL).replaceFirst("https", "git")).setDirectory(new File(userdata.get(UserData.FILE_PATH)));
                command.setProgressMonitor(monitor);
                monitor.start(1);
                workthread = new Thread(() -> {
                    try {
                        command.call();
                        gitoutputmessage.setText("downloading files : " + "done !");
                        
                        
                    } catch (GitAPIException e) {
                        e.printStackTrace();
                    } finally {
                        if(!cancelled){
                            if(createprofile){
                                writeProfileToFile(workthread);
                                copyMCInfos(workthread);
                            } else {
                                progress += 0.2;
                                progressbar.setProgress(progress);
                                progresspercenttext.setText(((int)(progress*100))+"%");
                            }
                            progressbar.setProgress(1);
                            progresspercenttext.setText(((100))+"%");
                            progresspercenttext.setFill(Paint.valueOf("#0093c3"));
                            installingshowtext.setText("Successfully Installed");
                            closebutton.setDisable(false);
                            closebutton.setVisible(true);
                        } else {
                            setCancelled();
                        }
                        
                        monitor.endTask();
                    }
                });
                workthread.start();
            }
            
            
        }
        
            //FileUtils.delete(new File(userdata.get(UserData.FILE_PATH)), FileUtils.RECURSIVE);
        
        
    
        checkInfos(urlfield.getText(), filefield.getText(), userdata.get(UserData.FILE_PATH), userdata.get(UserData.MODPACK_URL));
    }
    
    public void checkInfos(String urlinput, String fileinput, String finaldestination, String finalurl) {
        System.out.println('\n'+"File path : "+userdata.get(UserData.FILE_PATH));
        System.out.println("URL : "+userdata.get(UserData.MODPACK_URL));
        
        System.out.println("urlfield : "+urlfield.getText());
        System.out.println("filefield : "+filefield.getText());
    }
    
    public static HashMap<UserData, String> getUserData(){
        return userdata;
    }
    
    public void openBrowser(ActionEvent actionEvent) {
        DirectoryChooser directorychooser = new DirectoryChooser();
        File minecraftdirectory = mc_root;
        if(minecraftdirectory.exists()) directorychooser.setInitialDirectory(minecraftdirectory);
        
        String directory = directorychooser.showDialog(browsebutton.getScene().getWindow()).toString();
        userdata.put(UserData.FILE_PATH, directory);
        filefield.setText(userdata.get(UserData.FILE_PATH));
    }
    
    public void setModpackUrl(ActionEvent actionEvent) {
        try{
            URL url = new URL(urlfield.getText());
            StringBuilder checkgit = new StringBuilder();
            
            for(int i = url.toString().length()-4; i <= url.toString().length()-1; i++){
                checkgit.append(url.toString().toCharArray()[i]);
            }
            if(!checkgit.toString().equals(".git")){
                throw new UnsupportedAddressTypeException();
            }
    
            for(int i = url.toString().length()-1; i >= 0; i--){
                if(url.toString().toCharArray()[i] == '/' || url.toString().toCharArray()[i] == '\\') break;
                profilename.append(url.toString().toCharArray()[i]);
            }
            profilename.reverse();
            profilename = new StringBuilder(profilename.toString().replaceAll(".git", ""));
            System.out.println(profilename);
            displayDefaultMessage();
            
            urlerrormessage.setTextFill(Paint.valueOf("#008000"));
            urlerrormessage.setText("✓");
            userdata.put(UserData.MODPACK_URL, urlfield.getText());
            filefield.requestFocus();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            urlfield.setText("");
            urlerrormessage.requestFocus();
            urlerrormessage.setTextFill(Paint.valueOf("#B22222"));
            urlerrormessage.setText("this is not a valid URL");
            
        } catch (UnsupportedAddressTypeException e2){
            e2.printStackTrace();
            urlfield.setText("");
            urlerrormessage.requestFocus();
            urlerrormessage.setTextFill(Paint.valueOf("#B22222"));
            urlerrormessage.setText("this is not a valid GIT repository");
        }
    }
    
    public void setModpackDirectory(ActionEvent actionEvent) {
        File check = new File(filefield.getText());
        if(check.exists() && check.isDirectory()){
            if(Objects.requireNonNull(check.list()).length > 1){
             fileerrormessage.setText("Big folder, be carefull");
            } else {
             fileerrormessage.setText("");
            }
        }
    }
    
    public void setDefaultMessage(MouseEvent mouseEvent) {
        displayDefaultMessage();
    }
    
    public void toggleCreateProfile(ActionEvent actionEvent) {
        if(profilecheck.isSelected()){
            createprofile = true;
            
            copyinfosbox.setVisible(true);
            copyinfosbox.setDisable(false);
    
            copymcsettings.setSelected(true);
            copymcrp.setSelected(true);
            copymcshaders.setSelected(true);
        } else {
            createprofile = false;
            
            copyinfosbox.setVisible(false);
            copyinfosbox.setDisable(true);
            
            copymcsettings.setSelected(false);
            copymcrp.setSelected(false);
            copymcshaders.setSelected(false);
        }
        displayDefaultMessage();
    }
    
    public void toggleCopyInfos(ActionEvent actionEvent) {
    }
    
    private void displayDefaultMessage(){
        if(new File(mc_root+"\\profiles\\").exists()){
            File minecraftdirectory = mc_root;
            
                 minecraftdirectory = new File(mc_root +"\\profiles\\"+profilename.toString());
            
            defaultfoldermessage.setText("default location : "+ minecraftdirectory);
        } else {
            new File(mc_root+"\\profiles\\").mkdir();
        }
    }
    
    public void cancelDownload(ActionEvent actionEvent) {
        cancelled = true;
        if(workthread.isAlive()){
            workthread.interrupt();
        }
        setCancelled();
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
        
        userdata.put(UserData.JVM, jvmargsfield.getText());
        versionselect.requestFocus();
    }
    
    public void moreoption_goto(MouseEvent mouseEvent) {
        System.out.println("indeed");
    }
    
    public void copyMCInfos(Thread thread){
        File profiledirectory = new File(userdata.get(UserData.FILE_PATH)+"\\");
        File settings = new File(mc_root+"\\options.txt");
        File settingsoptifine = new File(mc_root+"\\optionsof.txt");
        File resourcepacks = new File(mc_root+"\\resourcepacks");
        File shaderpacks = new File(mc_root+"\\shaderpacks");
        
        if(copymcsettings.isSelected() && thread.isAlive() && !cancelled){
            gitoutputmessage.appendText("\ncopying settings : copying...");
            try{
                if(settings.exists() && profiledirectory.exists() && thread.isAlive() && !cancelled){
                    org.apache.commons.io.FileUtils.copyFileToDirectory(settings, profiledirectory);
                }
                if(settingsoptifine.exists() && profiledirectory.exists() && thread.isAlive() && !cancelled){
                    org.apache.commons.io.FileUtils.copyFileToDirectory(settingsoptifine, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            gitoutputmessage.setText(gitoutputmessage.getText().replaceAll("copying settings : copying...", "copying settings : done !"));
        }
        progress += 0.1/3f;
        progressbar.setProgress(progress);
        progresspercenttext.setText(((int)(progress*100))+"%");
        
        if(copymcrp.isSelected() && thread.isAlive() && !cancelled){
            gitoutputmessage.appendText("\ncopying resource packs : copying...");
            try{
                if(resourcepacks.exists() && profiledirectory.exists() && thread.isAlive() && !cancelled){
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(resourcepacks, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            gitoutputmessage.setText(gitoutputmessage.getText().replaceAll("copying resource packs : copying...", "copying resource packs : done !"));
        }
        progress += 0.1/3f;
        progressbar.setProgress(progress);
        progresspercenttext.setText(((int)(progress*100))+"%");
        
        if(copymcshaders.isSelected() && thread.isAlive() && !cancelled){
            gitoutputmessage.appendText("\ncopying shader packs : copying...");
            try{
                if(shaderpacks.exists() && profiledirectory.exists() && thread.isAlive() && !cancelled){
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(shaderpacks, profiledirectory);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            gitoutputmessage.setText(gitoutputmessage.getText().replaceAll("copying shader packs : copying...", "copying shader packs : done !"));
        }
        if(thread.isAlive() && !cancelled){
            progress += 0.1/3f;
            progressbar.setProgress(progress);
            progresspercenttext.setText(((int)(progress*100))+"%");
    
            gitoutputmessage.appendText("\n\nInstallation process finished !");
            cancelldownload.setDisable(true);
        }
        if(cancelled){
                setCancelled();
        }
    }
    
    public void writeProfileToFile(Thread thread){
        
        if(thread.isAlive() && !cancelled){
            try{
                gitoutputmessage.appendText("\ncreating profile : creating");
                Gson gson = new Gson();
                JsonObject jsonobj = gson.fromJson(new FileReader(profilesfile), JsonObject.class);
                File file = new File(mc_root+"\\launcher_profiles_SMIbackup.json");
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
                map.put("gameDir", userdata.get(UserData.FILE_PATH));
                map.put("icon", "data:image/png;base64,"+ image);
                map.put("javaArgs", userdata.get(UserData.JVM));
                map.put("lastUsed", time.toString());
                map.put("lastVersionId", userdata.get(UserData.VERSION));
                map.put("name", profilename.toString());
                map.put("type", "custom");
        
        
                jsonobj.get("profiles").getAsJsonObject().add(String.valueOf(UUID.randomUUID()), JsonParser.parseString(gson.toJson(map)));
        
        
                try(FileOutputStream outs = new FileOutputStream(profilesfile)){
                    outs.write(new GsonBuilder().setPrettyPrinting().create().toJson(jsonobj).getBytes());
                }
        
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                gitoutputmessage.setText(gitoutputmessage.getText().replaceAll("\ncreating profile : creating", "\ncreating profile : done !"));
            }
        }
        progress += 0.1;
        progressbar.setProgress(progress);
        progresspercenttext.setText(((int)(progress*100))+"%");
    }
    
    private void setCancelled(){
        progresspercenttext.setText("cancelled");
        progresspercenttext.setFill(Paint.valueOf("#b22222"));
        progressbar.setProgress(1);
        if(!gitoutputmessage.getText().contains("Installation process cancelled")){
            gitoutputmessage.appendText("\n\nInstallation process cancelled");
        }
        cancelldownload.setDisable(true);

        new Thread(() -> {
            try {
                File file = new File(userdata.get(UserData.FILE_PATH));
                if(file.exists()){
                    try{
                        FileUtils.deleteDirectory(file);
                    } finally {
                        if(new File(userdata.get(UserData.FILE_PATH)).exists()){
                            new File(userdata.get(UserData.FILE_PATH)).delete();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public void closeButtonPressed(ActionEvent actionEvent) {
        Platform.exit();
    }
}
