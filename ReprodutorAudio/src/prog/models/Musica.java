/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prog.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Avell
 */
public class Musica {
    private IntegerProperty id;
    private StringProperty nome;
    private StringProperty caminho;

    public Musica(int id, String nome, String caminho) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.caminho = new SimpleStringProperty(caminho);
    }

    @Override
    public String toString() {
        return "Musica{" + "id=" + id.getValue() + ", nome=" + nome.getValue() + ", caminho=" + caminho.getValue() + '}';
    }
    
    public IntegerProperty getId() {
        return id;
    }

    public void setId(IntegerProperty id) {
        this.id = id;
    }

    public StringProperty getNome() {
        return nome;
    }

    public void setNome(StringProperty nome) {
        this.nome = nome;
    }

    public StringProperty getCaminho() {
        return caminho;
    }

    public void setCaminho(StringProperty caminho) {
        this.caminho = caminho;
    }
    
    
}
