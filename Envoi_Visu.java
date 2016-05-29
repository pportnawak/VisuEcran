import java.io.*; //
import java.util.Properties; //

import javafx.application.Application; //
import javafx.application.Platform; //
import javafx.stage.Stage; //
import javafx.stage.WindowEvent; //
import javafx.scene.Scene; //
import javafx.scene.control.Button; //
import javafx.scene.control.TextField; //
import javafx.scene.layout.GridPane; //
import javafx.scene.layout.BorderPane; //
import javafx.scene.text.Text; //
import javafx.scene.text.Font; //
import javafx.scene.text.FontWeight; //
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent; //
import javafx.scene.web.WebEngine; //
import javafx.scene.web.WebView; //
import javafx.scene.effect.Reflection; //
import javafx.scene.layout.VBox; //

import javafx.geometry.Insets; //

import javafx.collections.ObservableList; //
import javafx.collections.FXCollections; //
import javafx.event.EventHandler; //
import javafx.event.ActionEvent; //

import com.jcraft.jsch.Channel; //
import com.jcraft.jsch.ChannelExec; //
import com.jcraft.jsch.JSch; //
import com.jcraft.jsch.Session; //
 
public class Envoi_Visu extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
       @Override
       public void handle(WindowEvent e) {
          Platform.exit();
          System.exit(0);
       }
    });

/*
 * lecture du fichier ini : lu en iso8859-1 doit être converti en utf-8
 */
    Properties mainProperties = new Properties();  
    try{        
    //to load application's properties, we use this class
    String path = "./Parodiprg.properties";
    InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "UTF-8");
    mainProperties.load(isr);
    isr.close();
            }catch(Exception e){
        return;
    } 
/*
 * variables définies pour plus de lisibilité
 */
   String cheminweb = mainProperties.getProperty("cheminweb");   
/*
 * Titre
 */
   primaryStage.setTitle("Ecran \" " + mainProperties.getProperty("TVNum") + " \" dans salle \" " + mainProperties.getProperty("Salle") + " \".");
 /*
  * Affichage
  */  
   BorderPane border = new BorderPane();
   
   Text LMinutes = new Text("Minutes");
   LMinutes.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   TextField TMinutes = new TextField(mainProperties.getProperty("RebMn"));   
   TMinutes.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   Text LSecondes = new Text("Secondes");
   LSecondes.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   TextField TSecondes = new TextField(mainProperties.getProperty("RebSec"));
   TSecondes.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   Text LMessage = new Text("Message");
   LMessage.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   TextField  Message = new TextField(mainProperties.getProperty("Msg"));
   Message.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   Button btnGO = new Button("GO");
   btnGO.setFont(Font.font("Arial", FontWeight.BOLD, 20));
   btnGO.setOnAction(new EventHandler<ActionEvent>() {
        
            @Override
            public void handle(ActionEvent event) {
                /*
                 * rafraichissemnt de la page sur l'écran IP au clic sur bouton GO
                 */
                Visu_Go(mainProperties.getProperty("UserEcran"), mainProperties.getProperty("IP_Ecran"), mainProperties.getProperty("Passwd"), mainProperties.getProperty("RSAk"));
            }
        });
   Text LLieu = new Text("Ecran\n\r"  + mainProperties.getProperty("TVNum") + "\n\r" + "salle\n\r" + mainProperties.getProperty("Salle") + "\n\r");
   LLieu.setFont(Font.font("Arial", FontWeight.BOLD, 20));

   ListView<String> list = new ListView<String>();
   list.setOnMouseClicked(new EventHandler<MouseEvent>(){
 
          @Override
          public void handle(MouseEvent arg0) {
              /*
               * visualisation de la page web sur clic sur la liste 
               */
              Visu_Ecran (list.getSelectionModel().getSelectedIndex(), TMinutes.getText(), TSecondes.getText(), Message.getText(), list.getSelectionModel().getSelectedItem(), mainProperties.getProperty("cheminweb"), border);
              //System.out.println("Visu_Ecran fini");
          }
 
   });
   ObservableList<String> items = FXCollections.observableArrayList (
     mainProperties.getProperty("Choix0") , mainProperties.getProperty("Choix1"), mainProperties.getProperty("Choix2"), mainProperties.getProperty("Choix3"), mainProperties.getProperty("Choix4"), mainProperties.getProperty("Texte0"), mainProperties.getProperty("Texte1"), mainProperties.getProperty("Texte2"), mainProperties.getProperty("Texte3"), mainProperties.getProperty("Texte4"), mainProperties.getProperty("Choix10"), mainProperties.getProperty("Choix11")  ); //data);
   list.setItems(items); 
/*
   liste des choix : panneau gauche
*/    
    border.setLeft(list);
     
/*
   grid : panneau central
   entrée des minutes, seconde et texte
   + bouton GO
*/
   GridPane grid = new GridPane();
   grid.setHgap(10);
   grid.setVgap(10);
   grid.setPadding(new Insets(0, 10, 0, 10));
   
   grid.add(LMinutes, 1, 0); 
   grid.add(TMinutes, 1, 1); 
   grid.add(LSecondes, 1, 2);
   grid.add(TSecondes, 1, 3);
   grid.add(LMessage, 1, 4);
   grid.add(Message, 1, 5);
   grid.add(btnGO, 4, 4);   
   border.setCenter(grid);
/*
   informations de la salle et de l'écran : panneau droit
*/
   border.setRight(LLieu);
 /*
   visu page web dans VBox: panenau du bas
*/  
     RechargeW(border, cheminweb);
/*
 * affichage de l'ensemble
 */
   primaryStage.setScene(new Scene(border, 1024, 768));
   primaryStage.show();
  }
    //Method mouseClicked for ListeChx
    private void Visu_Ecran(int ChxActuI, String mnr, String secr, String TMessage, String ChxListe, String cheminweb, BorderPane border) {
        /*
         *  Index de la liste, minutes, secondes, message entré, valeur de la liste (messages prédéfinis, etc.), chemin des fichiers, panneau d'affichage
         *  Selon le choix, fabrique/recopie le fichier cible.html puis l'affiche dans le panneau du bas de l'écran
         */
                   
            switch (ChxActuI) {
            
                case 0:
                case 4:
                case 10:
                    RecopyFile nf0 = new RecopyFile (cheminweb + "noir.html", cheminweb + "cible.html");
                    //LabelPanel2.setText("fichier : horloge --> cible.html" );
                    break;
                default : {
                    
                    try
                    {   
                        String nomframe = "message";
                        String TexteVu = "Aloha !";
                        String rebourscpt = Integer.toString(Integer.valueOf(mnr)*60 + Integer.valueOf(secr));
                        String finfich = "</marquee></body></html>";
                        switch (ChxActuI) {
                         case  1:
                            nomframe ="horloge";
                            RecopyFile nf = new RecopyFile (cheminweb + nomframe + ".html", cheminweb + "cible.html");
                           // LabelPanel2.setText("fichier : "+ nomframe + " --> cible.html" );
                            break; 
                         case  2:
                            nomframe ="rebours";
                            finfich ="\";t();</script></body></html> ";
                            Cree_Fichier (nomframe, rebourscpt, finfich, cheminweb);
                            break;
                         case  3:
                            nomframe ="reste";
                            finfich ="\";t();</script></body></html> ";
                            Cree_Fichier (nomframe, rebourscpt, finfich, cheminweb);
                            finfich ="";
                            break;
                         case 5:
                            TexteVu = TMessage;
                            Cree_Fichier (nomframe, TexteVu, finfich, cheminweb);
                            break;
                         case 6:
                         case 7:
                         case 8:
                         case 9:
                            TexteVu =  ChxListe; //list.getSelectionModel().getSelectedItem();
                            Cree_Fichier (nomframe, TexteVu, finfich, cheminweb);
                            break;
                         case 11:
                            nomframe ="image";
                            RecopyFile nf1 = new RecopyFile (cheminweb + nomframe + ".html", cheminweb + "cible.html");
                          //  LabelPanel2.setText("fichier : "+ nomframe + " --> cible.html" );
                            break;
                        }
                    } catch (Throwable t)
                    {
                        t.printStackTrace();
                }
                   //System.out.println("*** Visu Ecran : " + ChxActu  + "***");      
                    break;
                }
            }
            // affichage de la page web dans le panneau du bas
            RechargeW(border, cheminweb);
    }

    private void Cree_Fichier(String nomfich, String milieu, String finfich, String cheminweb) {
        /*
         * milieu = message ou temps venu des champs modifiables. finfich est une chaine qui fini le fichier html.
         * création du fichier pour le reste, compte à rebours et message (ceux avec des champs mofiables).
         */   
          String fsdeb = "";
          String debfich = cheminweb + nomfich +"deb";                 
          /*
           * lecture du fichier du début de la page web en bloc
           */                                            
          try{                  
              FileReader fr=new FileReader(debfich);
              BufferedReader br=new BufferedReader(fr);
              String s;
              while((s=br.readLine())!=null){
                    fsdeb+=s;
              }
              br.close();
          }           
          catch (Exception e){
              System.out.println(e.toString());
          }
          /*
           * création du fichier cible
           */
          File f = new File (cheminweb + "cible.html");
       
          try {
              PrintWriter pw = new PrintWriter (new BufferedWriter (new FileWriter (f)));
              /*
               * 
               */
              String result = fsdeb + milieu + finfich;
              pw.println (result);
              pw.close();            
          }
          catch (IOException exception) {
              System.out.println ("Erreur lors de la lecture : " + exception.getMessage());
          }   
          //String Panel2texte = "fichier : " + nomfich + " --> cible.";
          //LabelPanel2.setText(Panel2texte);
          //System.out.println (Panel2texte);
    }
                                 
    //Method mouseClicked for button1
    private void Visu_Go(String UserEcran, String IP_Ecran, String Passwd, String RSAk) {
        /*
         * visualisation sur l'ecran IP
         */
       
        //System.out.println("Visu_Go : début ...");       
        /*
         * outil xdotool (linux). Appui sur F5 dasn la fenêtre "Message" de Firefox = recharger la page web 
         * DISPLAY=:0 pour utiliser le client X11 i.e. le bureau graphique 
         */
        String cmd = "DISPLAY=:0 xdotool search \"Message\" windowactivate --sync key --clearmodifiers F5;";

        try{
        //System.out.println("Visu_Go : jsch ... " + UserEcran + "@" + IP_Ecran +" : " + RSAk + "..."); 
        /*
         * connection ssh
         */
        JSch jsch = new JSch();
        // envoi de la cle RSA format -----BEGIN RSA PRIVATE KEY----- xxxxxx -----END RSA PRIVATE KEY-----
        jsch.addIdentity(RSAk);
        //System.out.println("identity added");
        Session session=jsch.getSession(UserEcran, IP_Ecran, 22);
        // envoi du mot de passe si pas de cle RSA
        session.setPassword(Passwd);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        //System.out.println("Connected "+UserEcran+"@"+IP_Ecran);
        /*
         * envoi de la commande via le shell
         */
        ChannelExec channel=(ChannelExec) session.openChannel("exec");
        channel.setCommand(cmd);
        channel.connect();
        Channel channel1=session.openChannel("shell");//only shell  
        channel1.setOutputStream(System.out); 
        PrintStream shellStream = new PrintStream(channel1.getOutputStream());  // printStream for convenience 
        channel1.connect(); 
        shellStream.println(cmd); 
        shellStream.flush();
        Thread.sleep(5000);
        /*
         * fin connection
         */
        channel.disconnect();
        session.disconnect();
        if(channel.isClosed())
                 try{Thread.sleep(1000);}catch(Exception ee){System.out.println("Visu_Go : jsch closed");}
        }
        catch(Exception ee){System.out.println("Visu_Go : jsch erreur :" + ee);}
    }   
  
    private void RechargeW(BorderPane border, String cheminweb) {
        /*
         * charge la page web dans le bas du panneau
         */
         WebEngine engine;
         WebView view = new WebView();
         view.setZoom(0.25);
         engine = view.getEngine();
         Reflection reflection = new Reflection();
         reflection.setFraction(0.5);
         view.setEffect(reflection);
         // chargement page web
         engine.load("file://" + cheminweb + "cible.html");
         //System.out.println("RechargeW chemin : file://" + cheminweb + "cible.html");
         VBox VboxW = new VBox(view);
         VboxW.setPrefWidth(800);
         VboxW.setPrefHeight(375);
         // chargement dans le bas du panneau
         border.setBottom(VboxW);
    }
}