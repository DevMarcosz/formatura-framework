package br.ufal.ic.formatura.core.hotspot.pacote;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Fábrica/registro de pacotes (padrão <b>Factory</b> + registro extensível).
 *
 * <p>O framework já registra os pacotes padrão, mas a aplicação pode registrar
 * NOVOS pacotes em tempo de execução — outro ponto de extensão do hot spot 6,
 * sem modificar o código do framework (princípio Open-Closed).</p>
 */
public class FabricaPacote {

    private final Map<String, Supplier<Pacote>> registro = new LinkedHashMap<>();

    public FabricaPacote() {
        // Comportamento default do framework.
        registrar(PacoteFoto::new);
        registrar(PacoteVideo::new);
        registrar(PacoteAlbumLuxo::new);
    }

    /** Registra (ou pluga) um novo tipo de pacote. */
    public void registrar(Supplier<Pacote> fornecedor) {
        Pacote amostra = fornecedor.get();
        registro.put(amostra.codigo(), fornecedor);
    }

    /** Cria um pacote pelo código. */
    public Pacote criar(String codigo) {
        Supplier<Pacote> fornecedor = registro.get(codigo);
        if (fornecedor == null) {
            throw new IllegalArgumentException("Pacote não registrado: " + codigo);
        }
        return fornecedor.get();
    }

    /** Lista todos os pacotes disponíveis (um exemplar de cada). */
    public List<Pacote> disponiveis() {
        return registro.values().stream().map(Supplier::get).toList();
    }
}
