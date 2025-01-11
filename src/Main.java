import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean continueRunning = true;

            while (continueRunning) {
                // Moedas disponíveis
                String[] currencies = {"USD", "EUR", "COP", "CLP", "BRL"};

                System.out.println("\nBem-vindo ao Conversor de Moedas!");
                System.out.println("Escolha a sua moeda (número correspondente):");

                // Exibe o menu de opções
                for (int i = 0; i < currencies.length; i++) {
                    System.out.printf("%d - %s%n", i + 1, currencies[i]);
                }

                // escolhas do usuário
                int fromIndex = getUserChoice(scanner, currencies.length, "origem");
                System.out.println("Escolha a moeda de destino (número correspondente):");
                int toIndex = getUserChoice(scanner, currencies.length, "destino");

                System.out.println("Informe quanto deseja converter: ");
                double amount = scanner.nextDouble();

                // Moedas selecionadas
                String fromCurrency = currencies[fromIndex];
                String toCurrency = currencies[toIndex];


                double convertedAmount = convertCurrency(fromCurrency, toCurrency, amount);

                // Exibe o resultado
                if (convertedAmount != -1) {
                    System.out.printf("%.2f %s é equivalente a %.2f %s%n",
                            amount, fromCurrency, convertedAmount, toCurrency);
                } else {
                    System.out.println("Erro ao realizar a conversão. Verifique a API e tente novamente.");
                }

                // Verifica se o usuário deseja continuar ou encerrar
                continueRunning = !shouldExit(scanner);
            }

            System.out.println("Obrigado por usar o Conversor de Moedas.");
        }
    }

    // Método para obter a escolha do usuário
    private static int getUserChoice(Scanner scanner, int maxOption, String type) {
        int choice;
        while (true) {
            System.out.printf("Digite o número da moeda de %s: ", type);
            choice = scanner.nextInt() - 1;
            if (choice >= 0 && choice < maxOption) {
                break;
            } else {
                System.out.println("Escolha inválida. Tente novamente.");
            }
        }
        return choice;
    }

    // Método para conversão
    private static double convertCurrency(String fromCurrency, String toCurrency, double amount) {

        String apiUrl = "https://v6.exchangerate-api.com/v6/a361fb205093515024751e30/latest/" + fromCurrency;

        try {

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Verificar se a resposta foi bem-sucedida
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                // Verificar se o campo "conversion_rates" existe e contém a moeda de destino
                if (jsonResponse.has("conversion_rates")) {
                    JsonObject rates = jsonResponse.getAsJsonObject("conversion_rates");

                    if (rates.has(toCurrency)) {
                        double rate = rates.get(toCurrency).getAsDouble();
                        return amount * rate;
                    } else {
                        System.out.println("Erro: A moeda de destino não está disponível.");
                    }
                } else {
                    System.out.println("Erro: O campo 'conversion_rates' não foi encontrado na resposta da API.");
                }
            } else {
                System.out.println("Erro ao acessar a API. Código HTTP: " + response.statusCode());
                System.out.println("Resposta: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("Ocorreu um erro: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // Método para verificar se o usuário deseja encerrar
    private static boolean shouldExit(Scanner scanner) {
        System.out.println("\nDeseja realizar outra conversão? (S/N)");
        while (true) {
            String input = scanner.next().trim().toUpperCase();
            if (input.equals("N")) {
                return true; // Encerrar o programa
            } else if (input.equals("S")) {
                return false; // Continuar o programa
            } else {
                System.out.println("Entrada inválida. Por favor, digite 'S' para sim ou 'N' para não.");
            }
        }
    }
}
