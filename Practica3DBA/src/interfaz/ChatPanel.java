/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatPanel extends JPanel {
    private JPanel chatPanel;
    private final JTextField inputField;
    private final JButton sendButton;

    public ChatPanel() {
        setLayout(new BorderLayout());

        // 1. Cabecera (Header) - Color gris oscuro
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 30, 30));
        JLabel headerLabel = new JLabel("Canal Seguro FIPA-ACL", SwingConstants.CENTER);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Área del Chat (Scroll)
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(25, 25, 25)); // Fondo del chat (Gris muy oscuro)
        
        JScrollPane chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(chatScrollPane, BorderLayout.CENTER);

        // 3. Panel de Entrada (Input)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(30, 30, 30));

        inputField = new JTextField();
        inputField.setBackground(new Color(50, 50, 50));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        sendButton = new JButton("Enviar");
        sendButton.setBackground(new Color(37, 211, 102)); // Verde tipo WhatsApp
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(inputPanel, BorderLayout.SOUTH);

        // 4. Eventos
        sendButton.addActionListener((ActionEvent e) -> sendMessage());
        inputField.addActionListener((ActionEvent e) -> sendMessage());
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            mandarMensaje("Usuario", message); 
            inputField.setText("");
        }
    }

    // MÉTODO PÚBLICO
    public void mandarMensaje(String agenteName, String mensaje) {
        // Si es el Buscador o el Usuario, va a la derecha. El resto a la izquierda.
        boolean isRightSide = agenteName.equals("Buscador") || agenteName.equals("Usuario");
        addMessage(agenteName, mensaje, isRightSide);
    }

    private void addMessage(String sender, String message, boolean isRightSide) {
        
        // Panel contenedor para la fila (ocupa todo el ancho)
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Etiqueta del mensaje (Burbuja)
        // Usamos HTML para saltos de linea y negritas
        JLabel messageLabel = new JLabel("<html><b>" + sender + ":</b><br>" + message + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setOpaque(true); 
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        if (isRightSide) {
            // Alineado a DERECHA (Verde)
            messageLabel.setBackground(new Color(37, 211, 102)); 
            messageLabel.setForeground(Color.BLACK);
            rowPanel.add(messageLabel, BorderLayout.EAST);
        } else {
            // Alineado a IZQUIERDA (Blanco)
            messageLabel.setBackground(new Color(255, 255, 255));
            messageLabel.setForeground(Color.BLACK);
            rowPanel.add(messageLabel, BorderLayout.WEST);
        }

        chatPanel.add(rowPanel);
        
        // Truco para hacer auto-scroll hacia abajo
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        
        chatPanel.revalidate();
        chatPanel.repaint();
    }
}
