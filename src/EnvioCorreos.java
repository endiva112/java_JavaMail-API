import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;

public class EnvioCorreos {

    // ══════════════════════════════════════════════════════════════════════════
    //  VARIABLES GLOBALES
    // ══════════════════════════════════════════════════════════════════════════
    private static String emailUsuario;
    private static String claveUsuario;

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {

        System.out.println("==========================================");
        System.out.println("   IESMARISMA - Envío Automático de Correos");
        System.out.println("==========================================\n");

        // 1. Solicitar credenciales
        solicitarCredenciales();

        // 2. Leer destinatarios
        List<String> destinatarios = leerDestinatarios("Clientes.txt");
        if (destinatarios.isEmpty()) {
            System.out.println("Error: No se encontraron destinatarios en Clientes.txt. Saliendo...");
            return;
        }
        System.out.println("✅ " + destinatarios.size() + " destinatario(s) cargados.\n");

        // 3. Leer mensaje
        String mensaje = leerMensaje("Mensaje.txt");
        if (mensaje.isEmpty()) {
            System.out.println("Error: El archivo Mensaje.txt está vacío o no existe. Saliendo...");
            return;
        }
        System.out.println("✅ Mensaje cargado correctamente.\n");

        // 4. Enviar correos
        enviarCorreo(emailUsuario, claveUsuario, destinatarios, mensaje);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. SOLICITAR CREDENCIALES
    // ══════════════════════════════════════════════════════════════════════════
    public static void solicitarCredenciales() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Introduce tu dirección de Gmail: ");
        emailUsuario = scanner.nextLine().trim();

        // Intentar ocultar la contraseña si hay consola disponible (terminal real)
        Console console = System.console();
        if (console != null) {
            char[] pass = console.readPassword("Introduce tu contraseña (o App Password): ");
            claveUsuario = new String(pass);
        } else {
            // Fallback para IDEs donde System.console() es null
            System.out.print("Introduce tu contraseña (o App Password): ");
            claveUsuario = scanner.nextLine().trim();
        }

        System.out.println();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. LEER DESTINATARIOS
    // ══════════════════════════════════════════════════════════════════════════
    public static List<String> leerDestinatarios(String archivo) {
        List<String> listaDestinatarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    listaDestinatarios.add(linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: No se encontró el archivo \"" + archivo + "\".");
            System.out.println("       Asegúrese de que existe en el directorio de ejecución.");
        } catch (IOException e) {
            System.out.println("Error al leer \"" + archivo + "\": " + e.getMessage());
        }

        return listaDestinatarios;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. LEER MENSAJE
    // ══════════════════════════════════════════════════════════════════════════
    public static String leerMensaje(String archivo) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: No se encontró el archivo \"" + archivo + "\".");
            System.out.println("       Asegúrese de que existe en el directorio de ejecución.");
        } catch (IOException e) {
            System.out.println("Error al leer \"" + archivo + "\": " + e.getMessage());
        }

        return sb.toString().trim();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. CONFIGURAR SERVIDOR SMTP
    // ══════════════════════════════════════════════════════════════════════════
    public static Properties configurarServidorSMTP() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");   // TLS en puerto 587
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return props;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. ENVIAR CORREOS
    // ══════════════════════════════════════════════════════════════════════════
    public static void enviarCorreo(String remitente, String clave,
                                    List<String> destinatarios, String mensaje) {

        Properties props = configurarServidorSMTP();

        // Autenticación
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        System.out.println("Iniciando envío de correos...\n");
        int enviados = 0;
        int errores  = 0;

        for (String destinatario : destinatarios) {
            try {
                MimeMessage email = new MimeMessage(session);

                email.setFrom(new InternetAddress(remitente, "IESMARISMA"));
                email.addRecipient(Message.RecipientType.TO,  new InternetAddress(destinatario));
                // BCC al propio remitente para confirmar el envío
                email.addRecipient(Message.RecipientType.BCC, new InternetAddress(remitente));

                email.setSubject("Información de IESMARISMA", "UTF-8");
                email.setText(mensaje, "UTF-8");

                Transport.send(email);

                System.out.println("✅ Correo enviado a: " + destinatario);
                enviados++;

            } catch (MessagingException e) {
                System.out.println("❌ Error al enviar a " + destinatario + ": " + e.getMessage());
                errores++;
            } catch (Exception e) {
                System.out.println("❌ Error inesperado con " + destinatario + ": " + e.getMessage());
                errores++;
            }
        }

        System.out.println("\n==========================================");
        System.out.println("  Resumen del envío:");
        System.out.println("  ✅ Enviados correctamente : " + enviados);
        System.out.println("  ❌ Errores                : " + errores);
        System.out.println("==========================================");
    }
}