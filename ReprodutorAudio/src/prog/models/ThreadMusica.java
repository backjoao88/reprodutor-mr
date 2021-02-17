/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog.models;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javazoom.jl.decoder.JavaLayerException;
import prog.controllers.MainScreenController;

/**
 *
 * @author Avell
 */
public class ThreadMusica extends Thread {

    protected Musica musica;
    private FileInputStream arquivoMusica;
    private AudioPlayer tocador;
    private CheckBox chkFinaliza;

    public ThreadMusica(Musica musica, CheckBox chkFinaliza) throws FileNotFoundException, JavaLayerException {
        this.musica = musica;
        this.arquivoMusica = new FileInputStream(musica.getCaminho().getValue());
        this.chkFinaliza = chkFinaliza;
        tocador = new AudioPlayer(arquivoMusica, chkFinaliza);
    }   

    public CheckBox getChkFinaliza() {
        return chkFinaliza;
    }

    public void setChkFinaliza(CheckBox chkFinaliza) {
        this.chkFinaliza = chkFinaliza;
    }

    public Musica getMusica() {
        return musica;
    }

    public void setMusica(Musica musica) {
        this.musica = musica;
    }

    public FileInputStream getArquivoMusica() {
        return arquivoMusica;
    }

    public void setArquivoMusica(FileInputStream arquivoMusica) {
        this.arquivoMusica = arquivoMusica;
    }

    public AudioPlayer getTocador() {
        return tocador;
    }

    public void setTocador(AudioPlayer tocador) {
        this.tocador = tocador;
    }
    
    public void aumentarVolume(){
        if(VolumeControl.getVolume() < (float) 1.1) VolumeControl.setVolume(VolumeControl.getVolume() + (float) 0.1);
    }
    
    public void diminuirVolume(){
        if(VolumeControl.getVolume() > (float) -0.1) VolumeControl.setVolume(VolumeControl.getVolume() - (float) 0.1);
    }

    @Override
    public void run() {
        try {
            tocador.play();
        } catch (JavaLayerException ex) {
            Logger.getLogger(ThreadMusica.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
