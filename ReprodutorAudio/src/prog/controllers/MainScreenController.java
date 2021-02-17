/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.AccessibleAction;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import prog.models.Musica;
import prog.models.ThreadManipulacao;
import prog.models.ThreadMusica;
import prog.models.VolumeControl;

/**
 *
 * @author Avell
 */
public class MainScreenController implements Initializable {

    //variaveis internas
    private int sequenciaMusica;
    private ObservableList<Musica> listaMusica = FXCollections.observableArrayList();
    private Musica musicaAtual;
    private boolean trocouMusica = false;
    private boolean mudar = false;
    private ThreadMusica threadMusica;
    private int indiceMusica = -1;
    private ThreadManipulacao manipulacao;

    @FXML
    private TextField idMusica;

    @FXML
    private TextField nomeMusica;

    @FXML
    private Button botaoSelecionarMusica;

    @FXML
    private Button botaoNovaMusica;

    @FXML
    private Button botaoSalvarMusica;

    @FXML
    private Button botaoDeletarMusica;

    @FXML
    private TableView<Musica> tabLista = new TableView<Musica>();

    @FXML
    private TableColumn<Musica, Number> colId;

    @FXML
    private TableColumn<Musica, String> colNome;

    @FXML
    private TextField caminhoMusica;

    @FXML
    private Label musicaEmReproducao;

    @FXML
    private CheckBox arquivoLegenda;

    @FXML
    private TextField caminhoPlaylist;

    @FXML
    private TextArea txtLegenda;

    @FXML
    private ToggleButton btnModoAleatorio;

    @FXML
    private Button btnAnterior;

    @FXML
    private Button btnTocarPausar;

    @FXML
    private Button btnProxima;

    @FXML
    private Button btnParar;

    @FXML
    private MediaView mediaView;

    @FXML
    private Label contador;

    @FXML
    private CheckBox chkFinaliza;
    
    @FXML
    private Button btnAumentarVolume;
    
    @FXML
    private Button btnDiminuirVolume;
    
    @FXML
    void aoClicarAumentarVolume(ActionEvent event){
        threadMusica.aumentarVolume();
    }
    
    @FXML
    void aoClicarDiminuirVolume(ActionEvent event){
        threadMusica.diminuirVolume();
    }
    
   
    //manipulacao da musica
    @FXML
    void aoClicarAnterior(ActionEvent event) {
        int indiceNovo = 0;
        int totalMusicas = listaMusica.size();
        if (musicaAtual != null) {
            if (btnModoAleatorio.isSelected()) {
                indiceNovo = numeroAleatorio(0, totalMusicas - 1);
            } else {
                indiceNovo = indiceMusica == 0 ? totalMusicas - 1 : indiceMusica - 1;
            }
        } else {
            if (btnModoAleatorio.isSelected()) {
                indiceNovo = numeroAleatorio(0, totalMusicas - 1);
            } else {
                indiceNovo = 0;
            }
        }
        selecionarMusicaTocar(indiceNovo);
    }

    @FXML
    void aoClicarProxima(ActionEvent event) {
        cliqueProximaMusica();
    }

    void cliqueProximaMusica() {
        int indiceNovo = 0;
        int totalMusicas = listaMusica.size();
        if (musicaAtual != null) {
            if (btnModoAleatorio.isSelected()) {
                indiceNovo = numeroAleatorio(0, totalMusicas - 1);
            } else {
                indiceNovo = indiceMusica == totalMusicas ? 0 : indiceMusica + 1;
            }
        } else {
            if (btnModoAleatorio.isSelected()) {
                indiceNovo = numeroAleatorio(0, totalMusicas - 1);
            } else {
                indiceNovo = 0;
            }
        }
        selecionarMusicaTocar(indiceNovo);
    }

    private void selecionarMusicaTocar(int indice) {
        if (indice < 0 || indice >= listaMusica.size()) {
            indice = 0;
        }
        indiceMusica = indice;
        musicaAtual = listaMusica.get(indiceMusica);
        tabLista.getSelectionModel().select(musicaAtual);
        selecaoLinhaTabela(true);
    }

    @FXML
    void aoClicarParar(ActionEvent event) {
        if (threadMusica != null) {
            mudar = false;
            threadMusica.getTocador().close();
            manipulacao.zerar();
        }
        btnTocarPausar.setText("Tocar");
    }

    @FXML
    void aoModificarFinaliza(ActionEvent event) {
        if (mudar) {
            cliqueProximaMusica();
        }
    }

    @FXML
    void aoClicarTocarPausar(ActionEvent event) throws FileNotFoundException, JavaLayerException, IOException {
        try {
            tocarPausarMusica();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void tocarPausarMusica() throws FileNotFoundException, JavaLayerException, InterruptedException, IOException {
        String textoBotao = btnTocarPausar.getText();
        mudar = !trocouMusica;
        if (trocouMusica) {
            btnTocarPausar.setText("Pausar");
            if (threadMusica != null) {
                threadMusica.getTocador().close();
                manipulacao.zerar();
            }
            manipulacao = new ThreadManipulacao(contador, txtLegenda, musicaAtual.getCaminho().getValue(), arquivoLegenda.isSelected());
            threadMusica = new ThreadMusica(musicaAtual, chkFinaliza);
            threadMusica.start();
            manipulacao.iniciaContador();
            trocouMusica = false;
        } else {
            if (textoBotao.equals("Tocar")) {
                btnTocarPausar.setText("Pausar");
                if (threadMusica.getTocador().getPlayerStatus() == 2) {
                    threadMusica.getTocador().resume();
                    manipulacao.continuar();
                } else {
                    if (threadMusica != null) {
                        if (threadMusica.isAlive()) {
                            threadMusica.getTocador().close();
                        }
                    }
                    manipulacao = new ThreadManipulacao(contador, txtLegenda, musicaAtual.getCaminho().getValue(), arquivoLegenda.isSelected());
                    threadMusica = new ThreadMusica(musicaAtual, chkFinaliza);
                    threadMusica.start();
                    manipulacao.iniciaContador();
                }
            } else {
                //tava tocando
                btnTocarPausar.setText("Tocar");
                threadMusica.getTocador().pause();
                manipulacao.pausar();
            }
        }
        mudar = !trocouMusica;
    }

    // Manipulacao das pplaylists
    @FXML
    void aoClicarDeletarMusica(ActionEvent event) {
        if (!tabLista.getSelectionModel().isEmpty()) {
            Musica musicaSelecionada = tabLista.getSelectionModel().getSelectedItem();
            String nome = musicaSelecionada.getNome().getValue();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação Remoção");
            alert.setHeaderText(null);
            alert.setContentText("Você confirma a exclusão desta música " + nome + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                listaMusica.remove(musicaSelecionada);
            }
            this.aoClicarNovaMusica(event);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não há nenhuma linha selecionada");
            alert.showAndWait();
        }
    }

    @FXML
    void aoClicarNovaMusica(ActionEvent event) {
        VolumeControl.setVolume(99);
        idMusica.setText("");
        nomeMusica.setText("");
        caminhoMusica.setText("");
    }

    @FXML
    void aoClicarSalvarMusica(ActionEvent event) {
        boolean editando = !(idMusica.getText().isEmpty());
        int id;
        String nome = nomeMusica.getText();
        nome = nome.replaceAll("[^a-zA-Z0-9 _]", "");

        if (!nome.isEmpty()) {
            String caminho = caminhoMusica.getText();
            if (!caminho.isEmpty()) {
                if (editando) {
                    id = Integer.parseInt(idMusica.getText());
                    Musica musica = new Musica(id, nome, caminho);
                    listaMusica.set(tabLista.getSelectionModel().getSelectedIndex(), musica);
                } else {
                    id = this.sequenciaMusica;
                    this.sequenciaMusica++;
                    listaMusica.add(new Musica(id, nome, caminho));
                }
                this.aoClicarNovaMusica(event);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Nao foi encontrado um arquivo! Clique em 'Selecionar' para escolher um arquivo.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Este nome para a música não é válido!");
            alert.showAndWait();
        }
    }

    @FXML
    void aoClicarSelecionarMusica(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o arquivo de audio.");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3", "*.mp3"));
        File arqMusica = fileChooser.showOpenDialog(null);
        if (arqMusica != null) {
            caminhoMusica.setText(arqMusica.getAbsolutePath());
            nomeMusica.setText(arqMusica.getName().replaceAll(".mp3", ""));
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Arquivo selecionado não válido!");
            alert.showAndWait();
        }

    }

    @FXML
    void salvarPlaylist(ActionEvent event) throws IOException, URISyntaxException {
        String nomeArquivo = caminhoPlaylist.getText();
        //cria json
        JSONObject json = new JSONObject();
        JSONArray listaJson = new JSONArray();
        for (Musica musica : listaMusica) {
            JSONObject jsonElemento = new JSONObject();
            jsonElemento.put("id", musica.getId().getValue());
            jsonElemento.put("nome", musica.getNome().getValue());
            jsonElemento.put("caminho", musica.getCaminho().getValue());

            listaJson.put(jsonElemento);
        }
        json.put("Lista de Musicas", listaJson);

        //salva arquivos
        String textoJson = json.toString();

        if (nomeArquivo.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione o local para salvar o arquivo");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PLT", "*.plt"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                String[] splitCaminhoArquivo = file.getAbsolutePath().split("\\.");
                nomeArquivo = splitCaminhoArquivo[0] + ".plt";
            }
        }
        if (!nomeArquivo.isEmpty()) {
            File novoArquivo = new File(nomeArquivo);
            caminhoPlaylist.setText(nomeArquivo);

            //apos pegar caminho, salva arquivo
            BufferedWriter buffWrite = new BufferedWriter(new FileWriter(novoArquivo));
            buffWrite.append(textoJson);
            buffWrite.close();

        }
    }

    @FXML
    void salvarPlaylistComo(ActionEvent event) throws IOException {
        //cria json
        JSONObject json = new JSONObject();
        JSONArray listaJson = new JSONArray();
        for (Musica musica : listaMusica) {
            JSONObject jsonElemento = new JSONObject();
            jsonElemento.put("id", musica.getId().getValue());
            jsonElemento.put("nome", musica.getNome().getValue());
            jsonElemento.put("caminho", musica.getCaminho().getValue());

            listaJson.put(jsonElemento);
        }
        json.put("Lista de Musicas", listaJson);

        //salva arquivos
        String textoJson = json.toString();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o local para salvar o arquivo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PLT", "*.plt"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            String[] splitCaminhoArquivo = file.getAbsolutePath().split("\\.");
            String nomeArquivo = splitCaminhoArquivo[0] + ".plt";

            File novoArquivo = new File(nomeArquivo);
            caminhoPlaylist.setText(nomeArquivo);

            //apos pegar caminho, salva arquivo
            BufferedWriter buffWrite = new BufferedWriter(new FileWriter(novoArquivo));
            buffWrite.append(textoJson);
            buffWrite.close();
        }

    }

    @FXML
    void abrirPlaylist(ActionEvent event) throws FileNotFoundException, IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione a playlist.");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PLT", "*.plt"));
        File arqMusica = fileChooser.showOpenDialog(null);
        if (arqMusica != null) {
            FileReader arq = new FileReader(arqMusica.getPath());
            BufferedReader lerArq = new BufferedReader(arq);
            String linha = lerArq.readLine();
            String textoJson = linha;
            while (linha != null) {
                linha = lerArq.readLine(); // lê da segunda até a última linha
                textoJson += linha;
            }
            arq.close();
            listaMusica.clear();
            JSONObject json = new JSONObject(textoJson);
            JSONArray lista = json.getJSONArray("Lista de Musicas");

            int idUltima = 0;
            for (int i = 0; i < lista.length(); i++) {
                JSONObject jsonMusica = lista.getJSONObject(i);
                Musica musica = new Musica(jsonMusica.getInt("id"), jsonMusica.getString("nome"), jsonMusica.getString("caminho"));
                listaMusica.add(musica);
                if (musica.getId().getValue() > idUltima) {
                    idUltima = musica.getId().getValue();
                }
            }
            sequenciaMusica = idUltima+1;
            caminhoPlaylist.setText(arqMusica.getPath());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Arquivo selecionado não válido!");
            alert.showAndWait();
        }
    }

    @FXML
    void novaPlaylist(ActionEvent event) {
        listaMusica.clear();
        caminhoPlaylist.setText("");
        idMusica.setText("");
        caminhoMusica.setText("");
        nomeMusica.setText("");
    }

    @FXML
    void aoModificarMostraLegenda(ActionEvent event) {
        if (manipulacao != null) {
            manipulacao.setMostraLegenda(arquivoLegenda.isSelected());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.sequenciaMusica = 1;
        this.configuraTabela();
        tabLista.setItems(listaMusica);
        this.tableEvents();
    }

    private void configuraTabela() {
        colId.setCellValueFactory(cellData -> cellData.getValue().getId());
        colNome.setCellValueFactory(cellData -> cellData.getValue().getNome());
    }

    private void selecaoLinhaTabela(boolean duploClick) {

        if (!tabLista.getSelectionModel().isEmpty()) {
            Musica musica = tabLista.getSelectionModel().getSelectedItem();

            idMusica.setText(Integer.toString(musica.getId().getValue()));
            nomeMusica.setText(musica.getNome().getValue());
            caminhoMusica.setText(musica.getCaminho().getValue());

            if (duploClick) {
                File arquivo = new File(musica.getCaminho().getValue());
                if (arquivo.exists()) {
                    //musicaEmReproducao.setText(musica.getNome().getValue());
                    alteraNomeMusica(musica.getNome().getValue());
                    musicaAtual = musica;
                    indiceMusica = listaMusica.indexOf(musicaAtual);
                    trocouMusica = true;

                    String[] splitCaminhoArquivo = musica.getCaminho().getValue().split("\\.");
                    String nomeLegenda = splitCaminhoArquivo[0] + ".lgd";
                    File legenda = new File(nomeLegenda);
                    alteraArquivoLegenda(legenda.isFile());
                    /*if (legenda.isFile()) {
                        arquivoLegenda.setText("Arquivo de legenda encontrado, voce deseja mostra-lo?");
                        arquivoLegenda.setDisable(false);
                        arquivoLegenda.setSelected(true);
                    } else {
                        arquivoLegenda.setDisable(true);
                        arquivoLegenda.setText("Arquivo de legenda não encontrado, não será possível mostra-la!");
                        arquivoLegenda.setSelected(false);
                    }*/

                    try {
                        tocarPausarMusica();
                    } catch (JavaLayerException ex) {
                        Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    //musicaEmReproducao.setText("");
                    alteraNomeMusica("");
                    alteraArquivoLegenda(false);
                    /*arquivoLegenda.setDisable(true);
                    arquivoLegenda.setText("Arquivo de legenda não encontrado, não será possível mostra-la!");
                    arquivoLegenda.setSelected(false);*/
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Não foi possível encontrar a musica selecionada.");
                    alert.showAndWait();
                }
            }
        }

    }

    private void tableEvents() {
        this.tabLista.setOnMouseClicked(event -> {
            selecaoLinhaTabela(event.getClickCount() == 2);
        });
    }

    public int numeroAleatorio(int min, int max) {

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public void alteraArquivoLegenda(boolean temLegenda) {
        Thread t = new Thread(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (temLegenda) {
                        arquivoLegenda.setText("Arquivo de legenda encontrado, voce deseja mostra-lo?");
                        arquivoLegenda.setDisable(false);
                        arquivoLegenda.setSelected(true);
                    } else {
                        arquivoLegenda.setDisable(true);
                        arquivoLegenda.setText("Arquivo de legenda não encontrado, não será possível mostra-la!");
                        arquivoLegenda.setSelected(false);
                    }
                    if (manipulacao != null) {
                        manipulacao.setMostraLegenda(arquivoLegenda.isSelected());
                    }
                }
            });
        });
        t.start();
    }

    public void alteraNomeMusica(String nome) {
        Thread t = new Thread(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    musicaEmReproducao.setText(nome);
                }
            });
        });
        t.start();
    }

}
