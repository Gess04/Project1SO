/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaces;

import Process.Process;
import CPU.CPU;
import Scheduler.Scheduler;
import Settings.Settings;
import DS.Queue;
import DS.ProcessList;
import Clock.ClockManager;
import Scheduler.*;
import javax.swing.* ;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;
import Interfaces.HelperFunctions;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.util.Map;
import java.util.HashMap;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;



/**
 *
 * @author Gabriel Flores
 */
public class MainFrame extends javax.swing.JFrame {
    private int ioBoundcount =0;
    private int cpuBoundcount =0;
    
    private static Queue readyQueue = new Queue();
    private static Queue blockedQueue = new Queue();
    private static ProcessList exitList =  new ProcessList();
    private static Settings settings;
    private static CPU cpu;
    private static Scheduler scheduler;
    private static ClockManager clockManager;
    private SchedulingAlgorithm algorithm;
    
    private final JPanel readyPanel;         // panel interno dentro del JScrollPane de Listos
    private final JPanel blockedPanel;
    private final JPanel finishedPanel;
    private final JLabel clockLabel;
    private final AtomicInteger nextId = new AtomicInteger(1);
    private static final String CPU_BOUND = "CPU bound";
    private static final String IO_BOUND  = "I/O bound";
    private JLabel clockValue;
    
    private final Semaphore readyLock   = new Semaphore(1, true);
    private final Semaphore blockedLock = new Semaphore(1, true);
    private final Semaphore exitLock = new Semaphore(1, true);

    // --- Simulación de dispositivo de E/S ---
    // (solo un proceso puede usar el dispositivo a la vez)
    private final Semaphore ioDevice = new Semaphore(1, true);
    

    public MainFrame() {
     
        super("Simulador - NetBeans / Swing Example");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // Left controls panel
        JPanel left = new JPanel();
        left.setPreferredSize(new Dimension(320, 0));
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // Crear Proceso panel
        JPanel createPanel = new JPanel();
        createPanel.setBorder(new TitledBorder("Crear Proceso"));
        createPanel.setLayout(new GridBagLayout());
        createPanel.setPreferredSize(new Dimension(300, 200));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; createPanel.add(new JLabel("Nombre:"), c);
        c.gridx = 1; c.gridy = 0; JTextField nombreField = new JTextField(12); createPanel.add(nombreField, c);

        c.gridx = 0; c.gridy = 1; createPanel.add(new JLabel("Cant. Instrucciones:"), c);
        c.gridx = 1; c.gridy = 1; JSpinner instrSpinner = new JSpinner(new SpinnerNumberModel(1,1,1000,1)); createPanel.add(instrSpinner, c);

        c.gridx = 0; c.gridy = 2; createPanel.add(new JLabel("Tipo:"), c);
        c.gridx = 1; c.gridy = 2; JComboBox<String> tipoBox = new JComboBox<>(new String[]{"I/O bound", "CPU bound"}); createPanel.add(tipoBox, c);

        c.gridx = 0; c.gridy = 3; createPanel.add(new JLabel("Ciclos Excepción:"), c);
        c.gridx = 1; c.gridy = 3; JSpinner genEx = new JSpinner(new SpinnerNumberModel(1,1,100,1)); createPanel.add(genEx, c);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
        JButton crearBtn = new JButton("Crear Proceso");
        createPanel.add(crearBtn, c);

        left.add(createPanel);
        left.add(Box.createVerticalStrut(10));

        // Politica panel
        JPanel policyPanel = new JPanel();
        policyPanel.setBorder(new TitledBorder("Cambiar Politica de Planificacion"));
        policyPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> policyBox = new JComboBox<>(new String[]{"Round Robin", "FCFS", "SJF"});
        JButton savePolicy = new JButton("Guardar Cambios");
        policyPanel.add(policyBox);
        policyPanel.add(savePolicy);
        left.add(policyPanel);
        left.add(Box.createVerticalStrut(20));

        // Clock large label
        clockLabel = new JLabel("<html><div style='text-align:center'>Ciclo de reloj Global<br><span style='font-size:36px;'>0</span></div></html>");
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        clockLabel.setPreferredSize(new Dimension(260, 120));
        left.add(clockLabel);
        left.add(Box.createVerticalStrut(20));
   

        // CENTER: Colas
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // Cola de Listos (scroll horizontal)
        JPanel readyContainer = new JPanel(new BorderLayout());
        readyContainer.setBorder(new TitledBorder("Cola de Listos:"));
        readyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // panel donde se añaden tarjetas
        readyPanel.setMaximumSize(new Dimension(10,140));
        readyPanel.setBackground(new Color(245,245,245));
        JScrollPane readyScroll = new JScrollPane(readyPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        readyScroll.setPreferredSize(new Dimension(700,140));
        readyContainer.add(readyScroll, BorderLayout.CENTER);
        center.add(readyContainer);
        center.add(Box.createVerticalStrut(12));

        // Cola de Bloqueados
        JPanel blockedContainer = new JPanel(new BorderLayout());
        blockedContainer.setBorder(new TitledBorder("Cola de Bloqueados:"));
        blockedPanel = new JPanel();
        blockedPanel.setPreferredSize(new Dimension(700,160));
        blockedPanel.setBackground(Color.WHITE);
        JScrollPane blockedScroll = new JScrollPane(blockedPanel);
        blockedScroll.setPreferredSize(new Dimension(700,160));
        blockedContainer.add(blockedScroll, BorderLayout.CENTER);
        center.add(blockedContainer);
        center.add(Box.createVerticalStrut(12));

        // Cola de Terminados
        JPanel finishedContainer = new JPanel(new BorderLayout());
        finishedContainer.setBorder(new TitledBorder("Cola de Terminados:"));
        finishedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        finishedPanel.setBackground(new Color(245,245,245));
        JScrollPane finishedScroll = new JScrollPane(finishedPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        finishedScroll.setPreferredSize(new Dimension(700,140));
        finishedContainer.add(finishedScroll, BorderLayout.CENTER);
        center.add(finishedContainer);  
          
        
        initComponents();

        
        jLabel8.setText("Ciclos para generar excepción");
        completeExceptLabel.setText("Ciclos para satisfacer");
        
        // Refresca paneles cada 200 ms para ver Ready/Blocked/Exit y PC/MAR en vivo
        new javax.swing.Timer(200, e -> {
            refreshReadyPanel();
            refreshBlockedPanel();
            refreshFinishedPanel();
        }).start();
        
        // Reloj Global
        clockManager = new ClockManager(1);
       
        // Timer que reordena la readyQueue periódicamente (no bloquea al CPU)
        Timer reorderTimer = new Timer(120, e -> {
            scheduler.dispatch(cpu);
            scheduler.reorder();
            refreshReadyPanel(); // para que veas el reorden en la UI
        });
        reorderTimer.start();
        
        // CPU
        CPU cpu = new CPU(clockManager, readyQueue, blockedQueue, exitList,
                readyLock, blockedLock, exitLock, ioDevice);
        cpu.start();

        
        // Panel “Ciclo de reloj”
        jPanel17.setLayout(new GridBagLayout());
        jPanel17.setPreferredSize(new Dimension(200, 120)); // tamaño fijo
        clockValue = new JLabel("0", SwingConstants.CENTER);
        clockValue.setFont(new Font("SansSerif", Font.BOLD, 40));
        clockValue.setOpaque(true);
        clockValue.setBackground(new Color(240, 240, 240));
        clockValue.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        clockValue.setPreferredSize(new Dimension(120, 70)); // tamaño fijo del cuadro
        clockValue.setHorizontalAlignment(SwingConstants.CENTER);
        clockValue.setVerticalAlignment(SwingConstants.CENTER);
        jPanel17.add(clockValue);
        Timer uiClockTimer = new Timer(200, e -> clockValue.setText(String.valueOf(clockManager.getClockCycles())));
        uiClockTimer.start();
        
        Timer uiListsTimer = new Timer(300, e -> {
            scheduler.dispatch(cpu);
            refreshReadyPanel();
            refreshBlockedPanel();
            refreshFinishedPanel();
        });
        uiListsTimer.start();
        
        
        types1.addActionListener(evt -> {
            boolean isIO = IO_BOUND.equals(types1.getSelectedItem());
            cycles3.setEnabled(isIO);
            cycles4.setEnabled(isIO);
        });
        types1.setSelectedItem(CPU_BOUND);
        cycles3.setEnabled(false);
        cycles4.setEnabled(false);
    }
    
    static class ProcessCard extends JPanel {
        public ProcessCard(Process p, String estado) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            Dimension fixed = new Dimension(160, 120);
            setPreferredSize(fixed);
            setMinimumSize(fixed);
            setMaximumSize(fixed);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            setAlignmentX(Component.LEFT_ALIGNMENT);
            setAlignmentY(Component.TOP_ALIGNMENT);

            JLabel title = new JLabel(p.getProcessName());
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
            add(title);

            add(Box.createVerticalStrut(6));
            add(new JLabel("ID: " + p.getID()));
            add(new JLabel("PC: " + p.getPC()));
            add(new JLabel("MAR: " + p.getMAR()));
            add(new JLabel("Status: " + estado));
            add(new JLabel("Tipo: " + (p.isIObound() ? "I/O bound" : "CPU bound")));
        }
    }
    
    static class stadisticsFrame extends JPanel {
        private JLabel lblTotalTerminados, lblIOBoundTerminados, lblCPUBoundTerminados, lblCicloActual, lblThroughputGeneral;

    // --- Etiquetas por política ---
        private JLabel lblRRProcesos, lblRFCFSProcesos, lblSPNProcesos, lblSRTProcesos;
        public stadisticsFrame(){
            JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
            panel.setBorder(new TitledBorder("Estadísticas generales"));
            panel.setBackground(Color.WHITE);

            lblTotalTerminados = new JLabel("Procesos totales terminados: 0");
            lblIOBoundTerminados = new JLabel("Procesos I/O bound terminados: 0");
            lblCPUBoundTerminados = new JLabel("Procesos CPU bound terminados: 0");
            lblCicloActual = new JLabel("Ciclo actual: 0");
            lblThroughputGeneral = new JLabel("Throughput general: 0.0");

            panel.add(lblTotalTerminados);
            panel.add(lblIOBoundTerminados);
            panel.add(lblCPUBoundTerminados);
            panel.add(lblCicloActual);
            panel.add(lblThroughputGeneral);
        }
    }
    
    
    
    /**
     * Creates new form MainFrame
     */
    
        
       
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Simulador = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        nameProccess1 = new javax.swing.JTextField();
        instructionsCount1 = new javax.swing.JSpinner();
        types1 = new javax.swing.JComboBox<>();
        cycles3 = new javax.swing.JSpinner();
        cycles4 = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        createProcess = new javax.swing.JButton();
        completeExceptLabel = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        planningAlgorithm = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel10 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        cpuLabel1 = new javax.swing.JLabel();
        cpuLabel2 = new javax.swing.JLabel();
        cpuLabel3 = new javax.swing.JLabel();
        cpuLabel4 = new javax.swing.JLabel();
        cpuLabel5 = new javax.swing.JLabel();
        cpuLabel6 = new javax.swing.JLabel();
        cpuPLabel = new javax.swing.JLabel();
        cpuIDLabel = new javax.swing.JLabel();
        cpuPCLabel = new javax.swing.JLabel();
        cpuMARLabel = new javax.swing.JLabel();
        cpuStaLabel = new javax.swing.JLabel();
        cpuTipoLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        Graph3 = new javax.swing.JTabbedPane();
        jPanel9 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        PanelStadistics = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        g1 = new javax.swing.JPanel();
        lblFinishedProcess = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        g2 = new javax.swing.JLabel();
        g3 = new javax.swing.JLabel();
        g4 = new javax.swing.JLabel();
        g5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Crear Proceso", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        nameProccess1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameProccess1ActionPerformed(evt);
            }
        });

        types1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CPU bound", "I/O bound", " " }));
        types1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                types1ActionPerformed(evt);
            }
        });

        jLabel5.setText("Nombre:");

        jLabel6.setText("Cantidad de instr.");

        jLabel7.setText("Tipo:");

        jLabel8.setText("Ciclos para generar excepciones");

        createProcess.setText("Crear Proceso");
        createProcess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createProcessActionPerformed(evt);
            }
        });

        completeExceptLabel.setText("jLabel1");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(completeExceptLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(types1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(instructionsCount1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                            .addComponent(cycles4)))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cycles3, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(126, 126, 126))
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameProccess1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(172, 172, 172))
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(93, 93, 93)
                .addComponent(createProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameProccess1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instructionsCount1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(types1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cycles3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cycles4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(completeExceptLabel))
                .addGap(18, 18, 18)
                .addComponent(createProcess)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Configuraciones", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        planningAlgorithm.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Round robin", "FCFS", "Feedback", "HRRN", "SRT", "SPN"}));
        planningAlgorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                planningAlgorithmActionPerformed(evt);
            }
        });

        jButton4.setText("Guardar Cambios");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel1.setText("Políticas de planificación");

        jLabel2.setText("Duración de ciclos de ejecución");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(planningAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jSpinner2))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(planningAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addContainerGap())
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ciclo de reloj", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 96, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cola de listos", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 488, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel5);

        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cola de bloqueados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 486, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
        );

        jScrollPane2.setViewportView(jPanel8);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cola de terminados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 488, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(jPanel10);

        jButton1.setText("Crear 10 procesos");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CPU", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.ABOVE_TOP));

        cpuLabel1.setText("Proceso:");

        cpuLabel2.setText("ID:");

        cpuLabel3.setText("PC:");

        cpuLabel4.setText("MAR:");

        cpuLabel5.setText("Status:");

        cpuLabel6.setText("Tipo:");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                        .addComponent(cpuTipoLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cpuStaLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cpuMARLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cpuPCLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cpuIDLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cpuPLabel)))
                .addGap(62, 62, 62))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(cpuLabel1)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cpuLabel2)
                            .addComponent(cpuIDLabel))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cpuLabel3)
                            .addComponent(cpuPCLabel))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cpuLabel4)
                            .addComponent(cpuMARLabel))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cpuLabel5)
                            .addComponent(cpuStaLabel))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cpuLabel6)
                            .addComponent(cpuTipoLabel)))
                    .addComponent(cpuPLabel))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cola de suspendidos", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jScrollPane4.setViewportView(jScrollPane5);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(52, 52, 52)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(146, 146, 146)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(260, 260, 260))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(43, 43, 43)))
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32))))
        );

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(111, 111, 111))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        Simulador.addTab("Simulador", jPanel1);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 664, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(958, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(107, Short.MAX_VALUE))
        );

        Graph3.addTab("Comparacion I/O bound y CPU bound", jPanel9);

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1428, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 541, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(185, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Graph3.addTab("Comparacion tiempos de politicias", jPanel18);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Graph3)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(Graph3, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 88, Short.MAX_VALUE))
        );

        Simulador.addTab("Graficas", jPanel3);

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Estadísticas Generales", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel3.setText("Procesos totales terminados:");

        jLabel4.setText("Procesos I/O bound terminados:");

        jLabel9.setText("Procesos CPU bound termiandos:");

        jLabel10.setText("Ciclos de reloj:");

        jLabel11.setText("Throughput general:");

        javax.swing.GroupLayout g1Layout = new javax.swing.GroupLayout(g1);
        g1.setLayout(g1Layout);
        g1Layout.setHorizontalGroup(
            g1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 68, Short.MAX_VALUE)
        );
        g1Layout.setVerticalGroup(
            g1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        lblFinishedProcess.setText("0");

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        g2.setText("0");

        g3.setText("0");

        g4.setText("0");

        g5.setText("0");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblFinishedProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(379, 379, 379)
                        .addComponent(g1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(g4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addGap(100, 100, 100)
                                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(g2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(g3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(g5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(g1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                        .addComponent(lblFinishedProcess)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(g2))
                .addGap(18, 18, 18)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(g3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(g4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(g5))
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout PanelStadisticsLayout = new javax.swing.GroupLayout(PanelStadistics);
        PanelStadistics.setLayout(PanelStadisticsLayout);
        PanelStadisticsLayout.setHorizontalGroup(
            PanelStadisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelStadisticsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1344, Short.MAX_VALUE))
        );
        PanelStadisticsLayout.setVerticalGroup(
            PanelStadisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelStadisticsLayout.createSequentialGroup()
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 446, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PanelStadistics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanelStadistics, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        Simulador.addTab("Estadísticas", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Simulador, javax.swing.GroupLayout.PREFERRED_SIZE, 1628, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(Simulador, javax.swing.GroupLayout.PREFERRED_SIZE, 707, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        initalizeChart1();
        for (int i=1; i<11; i++) {
            int pId = i;
            String name = "Proceso " + Integer.toString(i);
            int instructionCount = 8;
            boolean type = (i%2 == 0) ? true : false;
            Integer cyclesToExcept = 0;
            Integer cyclesToCompleteRequest = 0;
            if (type) {
                cyclesToExcept = 3;
                cyclesToCompleteRequest = 2;
                ioBoundcount ++;
            } else {
                cpuBoundcount ++;
            }
            boolean CPUbound = !type;
            boolean IObound = type;
            int arrivalTime = clockManager.getClockCycles();

            Process process = new Process(pId, name,
                instructionCount, instructionCount,
                CPUbound, IObound,
                cyclesToExcept, cyclesToCompleteRequest,
                Process.Status.Ready, 0, 0, 0, null, arrivalTime, 0.0);

            try {
                readyLock.acquire();
                readyQueue.enqueue(process);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                readyLock.release();
            }
            updateChart();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        
        updateChart3();
        updateSchedulerAlgorithm();
        try {
            readyLock.acquire();              // BLOQUEA la cola mientras reordenas
            if (scheduler != null) {
                scheduler.reorder();          // reordena TODOS los que ya están en cola
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            readyLock.release();              
        }
        refreshReadyPanel();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void planningAlgorithmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_planningAlgorithmActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_planningAlgorithmActionPerformed

    private void createProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createProcessActionPerformed
        initalizeChart1();
        initalizeChart3();
        
        int pID = nextId.getAndIncrement();
        String name = nameProccess1.getText().toUpperCase().strip();
        String instructionsCounter = instructionsCount1.getValue().toString();
        String exceptionCycle = cycles3.getValue().toString();

        if (name.isEmpty()) {
            // Validación para que el nombre no sea vacío.
            javax.swing.JOptionPane.showMessageDialog(this, "Ingresa un nombre.");
            return;
        }

        Integer instructionCount = ((Number) instructionsCount1.getValue()).intValue();
        if (instructionCount < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "La cantidad de instrucciones del proceso debe ser mayor de 0.");
            return;
        }
        String type = (String) types1.getSelectedItem().toString();
        Integer cyclesToExcept = ((Number) cycles3.getValue()).intValue();
        Integer cyclesToCompleteRequest = ((Number) cycles4.getValue()).intValue();
        
        
        boolean isIO = IO_BOUND.equals(type);
        if (isIO) {
            ioBoundcount ++;
            if (cyclesToExcept <= 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Para I/O-bound, 'ciclos para generar E/S' debe ser > 0.");
                return;
            }
            if (cyclesToCompleteRequest < 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "La duración de E/S no puede ser negativa.");
                return;
            }
        } else {
            cyclesToExcept = 0;
            cyclesToCompleteRequest = 0;
            cpuBoundcount ++;
        }
        updateChart();

        boolean CPUbound = !isIO;
        boolean IObound = isIO;
        int arrivalTime = clockManager.getClockCycles();

        Process process = new Process(
            pID, name,
            instructionCount, instructionCount,
            CPUbound, IObound,
            cyclesToExcept, cyclesToCompleteRequest,
            Process.Status.Ready,
            0, 0,
            0,
            null,
            arrivalTime,
            0.0);
        process.start();

        try {
            readyLock.acquire();
            readyQueue.enqueue(process);
            if (scheduler != null) scheduler.reorder();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            readyLock.release();
        }

        nameProccess1.setText("");
        instructionsCount1.setValue(1);
        types1.setSelectedItem(CPU_BOUND);
        cycles3.setValue(1);
        cycles4.setValue(1);
        cycles3.setEnabled(false);
        cycles4.setEnabled(false);

        Process newProcess = new Process(name,instructionsCounter,exceptionCycle);
        newProcess.saveToCSV();
    }//GEN-LAST:event_createProcessActionPerformed

    private void types1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_types1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_types1ActionPerformed

    private void nameProccess1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameProccess1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameProccess1ActionPerformed
    
    private void refreshReadyPanel() {
        try {
            readyLock.acquire();
            Process[] arr = (Process[]) readyQueue.getAllElements();
            jPanel5.removeAll();
            jPanel5.setLayout(new BoxLayout(jPanel5, BoxLayout.X_AXIS));

            if (arr == null || arr.length == 0) {
                jPanel5.add(new JLabel("Vacío"));
            } else {
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        jPanel5.add(Box.createHorizontalStrut(10));
                    }
                    jPanel5.add(new ProcessCard(arr[i], "Ready"));
                }
            }
            jPanel5.revalidate();
            jPanel5.repaint();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            readyLock.release();
        }
    }

    private void refreshBlockedPanel() {
        try {
            blockedLock.acquire();
            Process[] arr = (Process[]) blockedQueue.getAllElements();
            jPanel8.removeAll();
            jPanel8.setLayout(new BoxLayout(jPanel8, BoxLayout.X_AXIS));

            if (arr == null || arr.length == 0) {
                jPanel8.add(new JLabel("Vacío"));
            } else {
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        jPanel8.add(Box.createHorizontalStrut(10));
                    }
                    jPanel8.add(new ProcessCard(arr[i], "Blocked"));
                }
            }
            jPanel8.revalidate();
            jPanel8.repaint();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            blockedLock.release();
        }
    }

    private void refreshFinishedPanel() {
        try {
            exitLock.acquire();
            Process[] arr = exitList.getAllProcesses();
            jPanel10.removeAll();
            jPanel10.setLayout(new BoxLayout(jPanel10, BoxLayout.X_AXIS));

            if (arr == null || arr.length == 0) {
                jPanel10.add(new JLabel("Vacío"));
            } else {
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        jPanel10.add(Box.createHorizontalStrut(10));
                    }
                    jPanel10.add(new ProcessCard(arr[i], "Exit"));
                    
                }
                
            }
            jPanel10.revalidate();
            jPanel10.repaint();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            exitLock.release();
        }
    }
    
    private void updateSchedulerAlgorithm() {
    String selected = (String) planningAlgorithm.getSelectedItem();
    int algorithm = (Integer) jSpinner2.getValue();

    if (selected == null) return;
    
    if (algorithm <= 0) {
        JOptionPane.showMessageDialog(this, "⚠️ Falta configurar los campos: el valor del parámetro debe ser mayor que 0.");
        System.out.println("Error: valor de parámetro inválido (" + algorithm + ")");
        return;
    }

    switch (selected) {
        case "FCFS":
            scheduler = new Scheduler(new FCFS(readyQueue), readyQueue);
            System.out.println("Algoritmo cambiado a FCFS");
            JOptionPane.showMessageDialog(this, "✅ Configuración guardada exitosamente.");
            break;

        case "SPN":
            scheduler = new Scheduler(new SPN(readyQueue), readyQueue);
            System.out.println("Algoritmo cambiado a SPN");
            JOptionPane.showMessageDialog(this, "✅ Configuración guardada exitosamente.");
            break;
            
        case "SJF":
            scheduler = new Scheduler(new SRT(readyQueue), readyQueue);
            System.out.println("Algoritmo cambiado a SRT");
            JOptionPane.showMessageDialog(this, "✅ Configuración guardada exitosamente.");
            break;

//        case "RR":
//            scheduler = new Scheduler(new RoundRobin(readyQueue, 3), readyQueue); // quantum=3 ejemplo
//            System.out.println("Algoritmo cambiado a Round Robin");
//            break;
            
            

        default:
            System.out.println("Algoritmo no reconocido.");
            JOptionPane.showMessageDialog(this, "❌ Error al guardar: ");
            break;
    }
    
}
    private JFreeChart chart1;
    DefaultPieDataset data = new DefaultPieDataset();
    public void initalizeChart1(){
        data.setValue("CPU Bound", 0);
        data.setValue("I/O Bound", 0);
    
        chart1 = ChartFactory.createPieChart(
                "I/O bound vs CPU bound", 
                data,
                true,
                true,
                false
        );
        ChartPanel panel = new ChartPanel(chart1);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(400,200));
    
        jPanel6.setLayout(new BorderLayout());
        jPanel6.removeAll();
        jPanel6.add(panel,BorderLayout.CENTER);
        
        pack();
        repaint();
        
    }
    
    public void updateChart() {
        if (data != null) {
        // Usando setValue con parámetros (Number, rowKey, columnKey)
        data.setValue("CPU bound", cpuBoundcount);
        data.setValue("I/O Bound", ioBoundcount);
        
        // Forzar actualización del gráfico
        chart1.fireChartChanged();
        jPanel6.revalidate();
        jPanel6.repaint();
        }
    }
    
    private JFreeChart chart3;
    DefaultCategoryDataset data3 = new DefaultCategoryDataset();
    public void initalizeChart3(){
        data3.addValue(0,"Tiempo de Ejecución" , "FCFS");
        data3.addValue(0,"Tiempo de Ejecución" , "Round Robin");
        data3.addValue(0,"Tiempo de Ejecución" , "SJF");
        data3.addValue(0, "Tiempo de Ejecución", "Feedback");
        data3.addValue(0, "Tiempo de Ejecución", "HRRN");
        data3.addValue(0, "Tiempo de Ejecución", "Scheduler");
        data3.addValue(0, "Tiempo de Ejecución", "SPN");
        
        chart3 = ChartFactory.createBarChart(
                "Comparación de Políticas de Planificación",
                "Politica",
                "Tiempo de ejecucion (s)",
                data3,
                PlotOrientation.VERTICAL,
                true,true,false
        );
        ChartPanel panel = new ChartPanel(chart3);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(800,500));
        CategoryPlot plot = chart3.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(1.0, 10.0);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    
        jPanel19.setLayout(new BorderLayout());
        jPanel19.removeAll();
        jPanel19.add(panel,BorderLayout.CENTER);
        jPanel19.revalidate();
        jPanel19.repaint();
        
        pack();
        repaint();
        
    }
    
    
    public void updateChart3() {
    // Obtiene los valores directamente de los componentes
    String algorithmName = (String) planningAlgorithm.getSelectedItem();
    int execTime = (Integer) jSpinner2.getValue();
    
    // Obtiene el valor actual del dataset para ese algoritmo
    Number currentValue = data3.getValue("Tiempo de Ejecución", algorithmName);
    int newTime = execTime;

    if (currentValue != null) {
        // Suma el nuevo tiempo al valor existente
        newTime += currentValue.intValue();
    }

    // Actualiza el dataset
    data3.setValue(newTime, "Tiempo de Ejecución", algorithmName);

    // Refresca la gráfica
    chart3.fireChartChanged();
    jPanel19.revalidate();
    jPanel19.repaint();
}

    
    public void updateFinishedProcessCount(int total) {
        lblFinishedProcess.setText(String.valueOf(total));
}

        
        
    
    /**
     * @param args the command line arguments
     */
    
   

    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            public void run() {
                new MainFrame().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane Graph3;
    private javax.swing.JPanel PanelStadistics;
    private javax.swing.JTabbedPane Simulador;
    private javax.swing.JLabel completeExceptLabel;
    private javax.swing.JLabel cpuIDLabel;
    private javax.swing.JLabel cpuLabel1;
    private javax.swing.JLabel cpuLabel2;
    private javax.swing.JLabel cpuLabel3;
    private javax.swing.JLabel cpuLabel4;
    private javax.swing.JLabel cpuLabel5;
    private javax.swing.JLabel cpuLabel6;
    private javax.swing.JLabel cpuMARLabel;
    private javax.swing.JLabel cpuPCLabel;
    private javax.swing.JLabel cpuPLabel;
    private javax.swing.JLabel cpuStaLabel;
    private javax.swing.JLabel cpuTipoLabel;
    private javax.swing.JButton createProcess;
    private javax.swing.JSpinner cycles3;
    private javax.swing.JSpinner cycles4;
    private javax.swing.JPanel g1;
    private javax.swing.JLabel g2;
    private javax.swing.JLabel g3;
    private javax.swing.JLabel g4;
    private javax.swing.JLabel g5;
    private javax.swing.JSpinner instructionsCount1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JLabel lblFinishedProcess;
    private javax.swing.JTextField nameProccess1;
    private javax.swing.JComboBox<String> planningAlgorithm;
    private javax.swing.JComboBox<String> types1;
    // End of variables declaration//GEN-END:variables
}
