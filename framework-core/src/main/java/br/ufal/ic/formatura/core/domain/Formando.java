package br.ufal.ic.formatura.core.domain;

/**
 * Representa um formando (aluno concluinte) participante de uma formatura.
 * Faz parte da PARTE INVARIANTE do framework: todo sistema de formatura,
 * independentemente do curso, possui formandos com estes dados básicos.
 */
public class Formando {

    private final String id;
    private final String nome;
    private final String curso;
    private final String email;
    private final String telefone;

    /** Renda declarada — usada por estratégias de rateio proporcional. */
    private double rendaDeclarada;

    /** Indica se o formando aderiu ao pacote de formatura (rateio por adesão). */
    private boolean aderiu;

    public Formando(String id, String nome, String curso, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.curso = curso;
        this.email = email;
        this.telefone = telefone;
        this.aderiu = true;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCurso() { return curso; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }

    public double getRendaDeclarada() { return rendaDeclarada; }
    public void setRendaDeclarada(double rendaDeclarada) { this.rendaDeclarada = rendaDeclarada; }

    public boolean isAderiu() { return aderiu; }
    public void setAderiu(boolean aderiu) { this.aderiu = aderiu; }

    @Override
    public String toString() {
        return nome + " (" + curso + ")";
    }
}
