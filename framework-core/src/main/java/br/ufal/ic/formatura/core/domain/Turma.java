package br.ufal.ic.formatura.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Turma de formandos de um curso/ano. Parte invariante do framework.
 */
public class Turma {

    private final String id;
    private final String curso;
    private final int ano;
    private final List<Formando> formandos = new ArrayList<>();

    public Turma(String id, String curso, int ano) {
        this.id = id;
        this.curso = curso;
        this.ano = ano;
    }

    public void adicionar(Formando formando) {
        formandos.add(formando);
    }

    /** Formandos que aderiram ao pacote de formatura. */
    public List<Formando> getAderentes() {
        List<Formando> aderentes = new ArrayList<>();
        for (Formando f : formandos) {
            if (f.isAderiu()) {
                aderentes.add(f);
            }
        }
        return aderentes;
    }

    public String getId() { return id; }
    public String getCurso() { return curso; }
    public int getAno() { return ano; }

    public List<Formando> getFormandos() {
        return Collections.unmodifiableList(formandos);
    }

    public int tamanho() { return formandos.size(); }
}
