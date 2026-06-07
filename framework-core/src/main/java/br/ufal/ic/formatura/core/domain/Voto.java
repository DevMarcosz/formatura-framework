package br.ufal.ic.formatura.core.domain;

/**
 * Voto de um formando em uma deliberação da comissão de formatura.
 * O peso é usado por regras de votação ponderadas (ex.: diretoria).
 */
public class Voto {

    private final String formandoId;
    private final String opcao;
    private final int peso;

    public Voto(String formandoId, String opcao) {
        this(formandoId, opcao, 1);
    }

    public Voto(String formandoId, String opcao, int peso) {
        this.formandoId = formandoId;
        this.opcao = opcao;
        this.peso = peso;
    }

    public String getFormandoId() { return formandoId; }
    public String getOpcao() { return opcao; }
    public int getPeso() { return peso; }
}
