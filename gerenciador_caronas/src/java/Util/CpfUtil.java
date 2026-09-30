package util;

public class CpfUtil {

    private CpfUtil() {
    }

    public static String limpar(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean valido(String cpf) {
        String numeros = limpar(cpf);

        if (numeros.length() != 11) {
            return false;
        }

        if (numeros.chars().distinct().count() == 1) {
            return false;
        }

        int[] digitos = numeros.chars().map(c -> c - '0').toArray();

        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += digitos[i] * (10 - i);
        }
        int primeiroDigito = (soma1 * 10) % 11;
        if (primeiroDigito == 10) {
            primeiroDigito = 0;
        }
        if (primeiroDigito != digitos[9]) {
            return false;
        }

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += digitos[i] * (11 - i);
        }
        int segundoDigito = (soma2 * 10) % 11;
        if (segundoDigito == 10) {
            segundoDigito = 0;
        }
        return segundoDigito == digitos[10];
    }

    public static String formatar(String cpf) {
        String numeros = limpar(cpf);
        if (numeros.length() != 11) {
            return cpf;
        }
        return numeros.substring(0, 3) + "." + numeros.substring(3, 6) + "."
                + numeros.substring(6, 9) + "-" + numeros.substring(9);
    }
}