/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;
import prog.controllers.MainScreenController;

/**
 *
 * @author Avell
 */
public class ThreadManipulacao extends Thread {

    private boolean iniciado;
    private boolean pausado;
    private int milissegundos;
    private Label contador;

    private TextArea txtLegenda;
    private boolean mostraLegenda;
    private int proximoMillissegundos;
    private String legenda;
    private JSONArray listaFrases;

    public TextArea getTxtLegenda() {
        return txtLegenda;
    }

    public void setTxtLegenda(TextArea txtLegenda) {
        this.txtLegenda = txtLegenda;
    }

    public boolean isMostraLegenda() {
        return mostraLegenda;
    }

    public void setMostraLegenda(boolean mostraLegenda) {
        this.mostraLegenda = mostraLegenda;
    }

    public int getProximoMillissegundos() {
        return proximoMillissegundos;
    }

    public void setProximoMillissegundos(int proximoMillissegundos) {
        this.proximoMillissegundos = proximoMillissegundos;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public JSONArray getListaFrases() {
        return listaFrases;
    }

    public void setListaFrases(JSONArray listaFrases) {
        this.listaFrases = listaFrases;
    }

    public boolean isIniciado() {
        return iniciado;
    }

    public void setInciado(boolean iniciado) {
        this.iniciado = iniciado;
    }

    public boolean isPausado() {
        return pausado;
    }

    public void setPausado(boolean executando) {
        this.pausado = executando;
    }

    public int getMilissegundos() {
        return milissegundos;
    }

    public void setMilissegundos(int milissegundos) {
        this.milissegundos = milissegundos;
    }

    public Label getContador() {
        return contador;
    }

    public void setContador(Label contador) {
        this.contador = contador;
    }

    public ThreadManipulacao(Label contador, TextArea txtLegenda, String caminhoLegenda, boolean mostraLegenda) throws IOException {
        this.contador = contador;
        this.iniciado = true;
        this.pausado = false;
        this.milissegundos = 0;
        mudaContador().start();

        this.txtLegenda = txtLegenda;

        String[] splitCaminhoArquivo = caminhoLegenda.split("\\.");
        String nomeLegenda = splitCaminhoArquivo[0] + ".lgd";
        File arquivoLegenda = new File(nomeLegenda);
        if (arquivoLegenda.isFile()) {
            BufferedReader lerArq = new BufferedReader(new FileReader(arquivoLegenda));
            String linha = lerArq.readLine();
            String textoJson = linha;
            while (linha != null) {
                linha = lerArq.readLine(); // lê da segunda até a última linha
                textoJson += linha;
            }
            JSONObject json = new JSONObject(textoJson);
            listaFrases = json.getJSONArray("letra");
            this.proximoMillissegundos = 1;
        }
        this.mostraLegenda = mostraLegenda;

    }

    public void zerar() {
        this.pausado = false;
        this.iniciado = false;
        this.milissegundos = 0;
        mudaContador().start();
    }

    public void pausar() {
        this.pausado = true;
    }

    public void continuar() {
        this.pausado = false;
        this.iniciaContador();
    }

    public void iniciaContador() {
        new Thread() {
            @Override
            public void run() {
                while (iniciado) {

                    if (!pausado) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (!pausado) {
                            milissegundos += 100;
                            verificaLegenda();
                            mudaContador().start();
                        }
                    }
                }
            }
        }.start();
    }

    public void verificaLegenda() {

        if (listaFrases != null) {
            if (listaFrases.length() > 0 && listaFrases.length() > proximoMillissegundos) {
                JSONObject fraseAtual = listaFrases.getJSONObject(proximoMillissegundos - 1);
                int miliIniAtual = converteMinMili(fraseAtual.getString("tempo_inicial"));
                int miliFinAtual = converteMinMili(fraseAtual.getString("tempo_final"));

                if (miliIniAtual <= milissegundos && miliFinAtual >= milissegundos) {
                    legenda = fraseAtual.getString("texto");
                } else if (miliFinAtual <= milissegundos) {
                    JSONObject novaFrase = listaFrases.getJSONObject(proximoMillissegundos);
                    proximoMillissegundos++;
                    int miliIniNovo = converteMinMili(fraseAtual.getString("tempo_inicial"));
                    int miliFinNovo = converteMinMili(fraseAtual.getString("tempo_final"));
                    if (miliIniNovo <= milissegundos && miliFinNovo >= milissegundos) {
                        legenda = novaFrase.getString("texto");
                    }
                }
            }
        } else {
            legenda = "...";
        }
        mudaLegenda();
    }

    public void mudaLegenda() {
        Thread t = new Thread(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (mostraLegenda) {
                        txtLegenda.setText(legenda);
                    } else {
                        txtLegenda.setText("...");
                    }
                }
            });
        });
        t.start();
    }

    public Thread mudaContador() {
        Thread t = new Thread(() -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    contador.setText(converteMiliMin(getMilissegundos()));
                }
            });
        });
        return t;
    }

    public String converteMiliMin(int milissegundos) {
        String retorno = "";
        int segundos = milissegundos / 1000;
        int minutos = segundos / 60;
        String min = Integer.toString(minutos);
        String seg = Integer.toString(abs(segundos - (minutos * 60)));
        min = min.length() == 1 ? ("0" + min) : min;
        seg = seg.length() == 1 ? ("0" + seg) : seg;
        retorno = min + " : " + seg;
        return retorno;
    }

    public int converteMinMili(String minutos) {
        int retorno = 0;
        String[] splitMinutos = minutos.split(":");
        retorno = (Integer.parseInt(splitMinutos[0]) * 60) + (Integer.parseInt(splitMinutos[1]));
        retorno = retorno * 1000;
        return retorno;
    }

}
