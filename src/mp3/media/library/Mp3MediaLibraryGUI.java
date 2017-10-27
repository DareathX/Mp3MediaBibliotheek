/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mp3.media.library;

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.swing.JOptionPane;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 *
 * @author sergi & wilco
 */
public class Mp3MediaLibraryGUI extends javax.swing.JFrame {

    FileInputStream FIS;
    BufferedInputStream BIS;
    public Player player;
    public long pauseLocation;
    public long songTotalLength;
    public String fileLocation;
    public boolean running = false;

    DefaultTableModel model;

    public Mp3MediaLibraryGUI() {
        initComponents();
        getFilesDetails();
        sorting();
        noSongs();
        bckclr();
        icon();
    }

    public void icon() {
        try {
            Image i = ImageIO.read(getClass().getResource("/icon/icon.png"));
            setIconImage(i);
        } catch (IOException ex) {
            Logger.getLogger(Mp3MediaLibraryGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sorting() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        jTableSongList.setRowSorter(sorter);
    }

    public void getFilesDetails() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        };
        File folder = new File(System.getProperty("user.home").concat("\\Music"));
        File[] listOfFiles = folder.listFiles(filter);
        model = (DefaultTableModel) jTableSongList.getModel();
        Object[] row = new Object[1];

        for (int i = 0; i < listOfFiles.length; i++) {

            try {
                InputStream input = new FileInputStream(listOfFiles[i]);
                ContentHandler handler = new DefaultHandler();
                Metadata metadata = new Metadata();
                Parser parser = new Mp3Parser();
                ParseContext parseCtx = new ParseContext();
                parser.parse(input, handler, metadata, parseCtx);
                input.close();

                row[0] = listOfFiles[i].getName();
                String title = metadata.get("title");
                String Artist = metadata.get("xmpDM:artist");
                String Genre = metadata.get("xmpDM:genre");
                String Year = metadata.get("xmpDM:releaseDate");

                model.addRow(row);
                jTableSongList.setValueAt(title, i, 1);
                jTableSongList.setValueAt(Artist, i, 2);
                jTableSongList.setValueAt(Genre, i, 3);
                jTableSongList.setValueAt(Year, i, 4);

            } catch (FileNotFoundException e) {
            } catch (IOException | SAXException | TikaException e) {
            }

        }
    }

    public void noSongs() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "There are no mp3 files found in the Music folder",
                    "Mp3 files not found", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    public void favorite() {
        int row = jTableSongList.getSelectedRow();

        if (jTableSongList.getValueAt(row, 0).toString().contains("★ - ")) {
            jTableSongList.setValueAt(jTableSongList.getValueAt(row, 0).toString().substring(4), row, 0);

        } else {
            String star = "★ - " + jTableSongList.getValueAt(row, 0).toString();
            jTableSongList.setValueAt(star, row, 0);
        }

    }

    public void search(String activeSearch) {
        TableRowSorter<DefaultTableModel> search = new TableRowSorter<>(model);
        jTableSongList.setRowSorter(search);
        search.setRowFilter(RowFilter.regexFilter("(?i)" + activeSearch));
    }

    public void stopSong() {
        if (player != null) {
            player.close();

            pauseLocation = 0;
            songTotalLength = 0;
            fileLocation = null;

        }
    }

    public void pauseSong() {
        if (player != null) {
            try {
                pauseLocation = FIS.available();
                player.close();
            } catch (IOException ex) {

            }
        }

    }

    public void playSong() {

        if (!running) {
            running = true;
            try {
                int row = jTableSongList.getSelectedRow();
                String path = jTableSongList.getValueAt(row, 0).toString();
                FIS = new FileInputStream(System.getProperty("user.home").concat("\\Music\\" + path));
                BIS = new BufferedInputStream(FIS);
                player = new Player(BIS);

                songTotalLength = FIS.available();

                fileLocation = path + "";

            } catch (FileNotFoundException | JavaLayerException ex) {

            } catch (IOException ex) {
                Logger.getLogger(Mp3MediaLibraryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        player.play();
                        running = false;
                    } catch (JavaLayerException ex) {
                        Logger.getLogger(Mp3MediaLibraryGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
        }
    }

    public void bckclr() {
        getContentPane().setBackground(new Color(224, 189, 35));
    }

    public void resumeSong() {

        if (!running) {
            running = true;
            try {
                FIS = new FileInputStream(System.getProperty("user.home").concat("\\Music\\" + fileLocation));
                BIS = new BufferedInputStream(FIS);
                player = new Player(BIS);

                FIS.skip(songTotalLength - pauseLocation);

            } catch (FileNotFoundException | JavaLayerException ex) {
            } catch (IOException ex) {
                Logger.getLogger(Mp3MediaLibraryGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        player.play();
                        running = false;
                    } catch (JavaLayerException ex) {
                        Logger.getLogger(Mp3MediaLibraryGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuFRD = new javax.swing.JPopupMenu();
        jMenuItemFavorite = new javax.swing.JMenuItem();
        jMenuItemRename = new javax.swing.JMenuItem();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenuItemRefresh = new javax.swing.JMenuItem();
        jButtonDelete = new javax.swing.JButton();
        jButtonRename = new javax.swing.JButton();
        jButtonFavorite = new javax.swing.JButton();
        jScrollPaneSongs = new javax.swing.JScrollPane();
        jTableSongList = new javax.swing.JTable();
        jTextFieldSearchField = new javax.swing.JTextField();
        jButtonRefresh = new javax.swing.JButton();
        jLabelSearch = new javax.swing.JLabel();
        jButtonPlay = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jButtonPause = new javax.swing.JButton();
        jButtonResume = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jMenuItemFavorite.setText("Favorite");
        jMenuItemFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFavoriteActionPerformed(evt);
            }
        });
        jPopupMenuFRD.add(jMenuItemFavorite);

        jMenuItemRename.setText("Rename");
        jMenuItemRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRenameActionPerformed(evt);
            }
        });
        jPopupMenuFRD.add(jMenuItemRename);

        jMenuItemDelete.setText("Delete");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jPopupMenuFRD.add(jMenuItemDelete);

        jMenuItemRefresh.setText("Refresh");
        jMenuItemRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRefreshActionPerformed(evt);
            }
        });
        jPopupMenuFRD.add(jMenuItemRefresh);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mp3 Library");
        setBackground(new java.awt.Color(224, 189, 35));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jButtonDelete.setText("Delete");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonRename.setText("Rename");
        jButtonRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRenameActionPerformed(evt);
            }
        });

        jButtonFavorite.setText("Favorite");
        jButtonFavorite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFavoriteActionPerformed(evt);
            }
        });

        jTableSongList.setBackground(new java.awt.Color(224, 189, 35));
        jTableSongList.setBorder(new javax.swing.border.MatteBorder(null));
        jTableSongList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Title", "Artist", "Genre", "Year"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSongList.setGridColor(new java.awt.Color(224, 189, 35));
        jTableSongList.setSelectionBackground(new java.awt.Color(224, 189, 35));
        jTableSongList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTableSongListMouseReleased(evt);
            }
        });
        jScrollPaneSongs.setViewportView(jTableSongList);

        jTextFieldSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldSearchFieldKeyReleased(evt);
            }
        });

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jLabelSearch.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabelSearch.setText("Search :");

        jButtonPlay.setText("Play");
        jButtonPlay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButtonPlayMouseReleased(evt);
            }
        });

        jButtonStop.setText("Stop");
        jButtonStop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButtonStopMouseReleased(evt);
            }
        });

        jButtonPause.setText("Pause");
        jButtonPause.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButtonPauseMouseReleased(evt);
            }
        });

        jButtonResume.setText("Resume");
        jButtonResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResumeActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/troll_face.png"))); // NOI18N
        jLabel1.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPaneSongs, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonRename, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonFavorite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonResume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonPause, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40)
                        .addComponent(jLabelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))))
            .addGroup(layout.createSequentialGroup()
                .addGap(267, 267, 267)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonRefresh)
                                    .addComponent(jButtonFavorite)
                                    .addComponent(jLabelSearch))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonRename)
                                    .addComponent(jButtonDelete)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonPause)
                                    .addComponent(jButtonPlay))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButtonStop)
                                    .addComponent(jButtonResume))))
                        .addGap(0, 7, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        jMenuItemDeleteActionPerformed(evt);
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        model = (DefaultTableModel) jTableSongList.getModel();
        int[] delete = jTableSongList.getSelectedRows();
        for (int i = 0; i < delete.length; i++) {
            model.removeRow((delete[i] - i));
        }
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jTableSongListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableSongListMouseReleased
        if (evt.isPopupTrigger()) {
            JTable popup = (JTable) evt.getSource();
            int row = popup.rowAtPoint(evt.getPoint());
            int column = popup.columnAtPoint(evt.getPoint());

            if (!popup.isRowSelected(row)) {
                popup.changeSelection(row, column, false, false);
            }
            jPopupMenuFRD.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jTableSongListMouseReleased

    private void jMenuItemRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRenameActionPerformed
        jTableSongList.editCellAt(jTableSongList.getSelectedRow(), 0);

    }//GEN-LAST:event_jMenuItemRenameActionPerformed

    private void jMenuItemFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFavoriteActionPerformed
        favorite();
    }//GEN-LAST:event_jMenuItemFavoriteActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshActionPerformed
        model.setRowCount(0);
        getFilesDetails();
        noSongs();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void jButtonFavoriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFavoriteActionPerformed
        favorite();
    }//GEN-LAST:event_jButtonFavoriteActionPerformed

    private void jButtonRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRenameActionPerformed
        jMenuItemRenameActionPerformed(evt);
    }//GEN-LAST:event_jButtonRenameActionPerformed

    private void jMenuItemRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRefreshActionPerformed
        jButtonRefreshActionPerformed(evt);
    }//GEN-LAST:event_jMenuItemRefreshActionPerformed

    private void jTextFieldSearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldSearchFieldKeyReleased
        String activeSearch = jTextFieldSearchField.getText();
        search(activeSearch);
    }//GEN-LAST:event_jTextFieldSearchFieldKeyReleased

    private void jButtonPlayMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonPlayMouseReleased
        playSong();
    }//GEN-LAST:event_jButtonPlayMouseReleased

    private void jButtonStopMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonStopMouseReleased
        stopSong();
    }//GEN-LAST:event_jButtonStopMouseReleased

    private void jButtonPauseMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonPauseMouseReleased
        pauseSong();
    }//GEN-LAST:event_jButtonPauseMouseReleased

    private void jButtonResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResumeActionPerformed
        resumeSong();
    }//GEN-LAST:event_jButtonResumeActionPerformed

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
            java.util.logging.Logger.getLogger(Mp3MediaLibraryGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Mp3MediaLibraryGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Mp3MediaLibraryGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Mp3MediaLibraryGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Mp3MediaLibraryGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonFavorite;
    private javax.swing.JButton jButtonPause;
    private javax.swing.JButton jButtonPlay;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JButton jButtonRename;
    private javax.swing.JButton jButtonResume;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelSearch;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemFavorite;
    private javax.swing.JMenuItem jMenuItemRefresh;
    private javax.swing.JMenuItem jMenuItemRename;
    private javax.swing.JPopupMenu jPopupMenuFRD;
    private javax.swing.JScrollPane jScrollPaneSongs;
    private javax.swing.JTable jTableSongList;
    private javax.swing.JTextField jTextFieldSearchField;
    // End of variables declaration//GEN-END:variables
}
