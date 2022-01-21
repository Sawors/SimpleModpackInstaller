package com.github.sawors.demo;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.*;



public class ModpackInstallControler {
    
    public TextField urlfield;
    public Button install;
    public TextField filefield;
    private static HashMap<UserData, String> userdata = new HashMap<>();
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
    public Button toggler;
    public Button toggler1;
    private boolean createprofile = true;
    private StringBuilder profilename = new StringBuilder();
    ArrayList<AnchorPane> layerlist = new ArrayList<>();
    
    public void initialize(){
        layerlist.add(datainput_LAYER);
        layerlist.add(installing_LAYER);
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
                
                userdata.put(UserData.FILE_PATH, System.getenv("APPDATA")+"\\.minecraft\\profiles\\"+profilename.toString());
                new File(userdata.get(UserData.FILE_PATH)).mkdirs();
            } else {
                userdata.put(UserData.FILE_PATH, System.getenv("APPDATA")+"\\.minecraft\\profiles\\");
                new File(userdata.get(UserData.FILE_PATH)).mkdirs();
            }
            
        } else {
            if(createprofile){
                userdata.put(UserData.FILE_PATH, filefield.getText()+"\\"+profilename.toString());
                new File(userdata.get(UserData.FILE_PATH)).mkdirs();
            }
        }
        File minecraftdirectory = new File(userdata.get(UserData.FILE_PATH));
        if(minecraftdirectory.exists()){
            if(createprofile){
                minecraftdirectory = new File(userdata.get(UserData.FILE_PATH));
                minecraftdirectory.mkdirs();
            }
        } else {
            installerrormessage.setText(installerrormessage.getText()+"\nThe minecraft directory specified does not exist.");
            Timer timer2 = new Timer("message2");
            try{
                timer2.schedule(new TimerTask()
                {
                    @Override
                    public void run(){
                        installerrormessage.setText(" ");
                    }
                }, 5000L);
            } finally {
                timer2.cancel();
            }
            return;
        }
        
        
        
        
        // EVERY CHECKS ARE MADE, START OF THE INSTALLATION PROCESS
        
        /*try{
            //FileUtils.delete(new File(userdata.get(UserData.FILE_PATH)), FileUtils.RECURSIVE);
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("installer-installing.fxml"));
                Scene scene2 = new Scene(fxmlLoader.load(), 640, 390);
                Stage stage = HelloApplication.getStage();
                stage.setTitle("Simple Modpack Installer v1.1 - Installing...");
                stage.setScene(scene2);
                HelloApplication.getStage().show();
                HelloApplication.getStage().requestFocus();
                File file = new File(userdata.get(UserData.FILE_PATH));
                for (int num = 0; file.exists(); num++) {
                    file = new File(userdata.get(UserData.FILE_PATH)+"_" + num);
                }
                userdata.put(UserData.FILE_PATH, file.toString());
                
                
                
            } finally {
                //https://github.com/Sawors/Stones_ResourcePack.git
                    //
                    
                    
                TimerTask task = new TimerTask() {
                    
                    public void run() {
                        try {
                            Writer writer = new StringWriter();
                            TextProgressMonitor monitor = new TextProgressMonitor(){
                                @Override
                                protected void onUpdate(String taskName, int workCurr) {
                                    super.onUpdate(taskName, workCurr);
                                }
    
                                @Override
                                protected void onEndTask(String taskName, int workCurr) {
                                    super.onEndTask(taskName, workCurr);
                                }
    
                                @Override
                                protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
                                    super.onUpdate(taskName, cmp, totalWork, pcnt);
                                    StringBuilder str = new StringBuilder("\n"+taskName + pcnt +" "+"/"+totalWork);
                                    HelloApplication.getInstallTextArea().setText(str.toString());
                                }
    
    
                                *//*
                                https://github.com/Sawors/Stones_Plugin.git
                                *//*
                                @Override
                                protected void onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
                                    super.onEndTask(taskName, cmp, totalWork, pcnt);
                                    //HelloApplication.getInstallTextArea().setText(taskName+cmp+pcnt);
                                }
                            };
    
                            BatchingProgressMonitor monitor2 = new BatchingProgressMonitor(){
    
                                @Override
                                protected void onUpdate(String s, int i) {
        
                                }
    
                                @Override
                                protected void onEndTask(String s, int i) {
        
                                }
    
                                @Override
                                protected void onUpdate(String s, int i, int i1, int i2) {
                                    try{
                                        Integer integ = i;
                                        HelloApplication.getInstallTextArea().setText(s+integ);
                                    } catch (NullPointerException e){
                                        HelloApplication.getInstallTextArea().setText("-");
                                    }
                                }
    
                                @Override
                                protected void onEndTask(String s, int i, int i1, int i2) {
        
                                }
                            };
                            CloneCommand command = Git.cloneRepository().setURI(userdata.get(UserData.MODPACK_URL).replaceFirst("https", "git")).setDirectory(new File(userdata.get(UserData.FILE_PATH)));
                            command.setProgressMonitor(monitor2);
                            monitor2.beginTask("download_process", 1024);
                            monitor2.update(2);
                            command.call();
                            monitor2.endTask();
                            
                            System.out.println("hey");
                        } catch (GitAPIException e) {
                            e.printStackTrace();
                        } finally {
                            this.cancel();
                        }
                    }
                };
                Timer timer = new Timer("Timer");
    
                long delay = 10L;
                timer.schedule(task, delay);
                
                
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        
    
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
        File minecraftdirectory = new File(System.getenv("APPDATA")+"\\.minecraft\\");
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
    
    public void toggleDirectory(ActionEvent actionEvent) {
        if(profilecheck.isSelected()){
            createprofile = true;
        } else {
            createprofile = false;
        }
        displayDefaultMessage();
    }
    
    private void displayDefaultMessage(){
        File minecraftdirectory = new File(System.getenv("APPDATA")+"\\.minecraft\\profiles\\");
        if(minecraftdirectory.exists()){
            if(createprofile){
                minecraftdirectory = new File(minecraftdirectory +"\\"+profilename.toString());
            }
            defaultfoldermessage.setText("default install location : "+minecraftdirectory);
            install.setCancelButton(false);
        } else {
            defaultfoldermessage.setText("default install location not found");
            install.setCancelButton(true);
        }
    }
    
    public void cancelDownload(ActionEvent actionEvent) {
    }
    
    public void toggleLayouts(ActionEvent actionEvent) {
        if(installing_LAYER.isDisabled() && !installing_LAYER.isVisible()){
            setLayer(installing_LAYER);
        } else {
            setLayer(datainput_LAYER);
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
}
