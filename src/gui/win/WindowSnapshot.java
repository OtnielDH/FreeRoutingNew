/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * SnapshotFrame.java
 *
 * Created on 9. November 2004, 09:42
 */

package gui.win;

import graphics.GdiCoordinateTransform;
import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.GuiResources;
import gui.varie.SnapSavedAttributes;
import interactive.SnapShot;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;

/**
 * Window handling snapshots of the interactive situation.
 *
 * @author Alfons Wirtz
 */
public class WindowSnapshot extends GuiSubWindowSavable
   {
   private static final long serialVersionUID = 1L;

   private DefaultListModel<SnapShot> list_model = new DefaultListModel<SnapShot>();
   private final JList<SnapShot> gui_list;
   private final javax.swing.JTextField name_field;
   final WindowSnapshotSettings settings_window;
   private int snapshot_count = 0;
   private final GuiResources resources;
   
   public WindowSnapshot(BoardFrame p_board_frame)
      {
      super(p_board_frame);
      
      this.settings_window = new WindowSnapshotSettings(p_board_frame);
      resources = board_frame.newGuiResources("gui.resources.WindowSnapshot");
      this.setTitle(resources.getString("title"));

      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      // create main panel
      final javax.swing.JPanel main_panel = new javax.swing.JPanel();
      add(main_panel);
      main_panel.setLayout(new java.awt.BorderLayout());

      // create goto button
      javax.swing.JButton goto_button = new javax.swing.JButton(resources.getString("goto_snapshot"));
      goto_button.setToolTipText(resources.getString("goto_tooltip"));
      GotoListener goto_listener = new GotoListener();
      goto_button.addActionListener(goto_listener);
      main_panel.add(goto_button, java.awt.BorderLayout.NORTH);

      // create snapshot list
      this.gui_list = new JList<SnapShot>(this.list_model);
      this.gui_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      this.gui_list.setSelectedIndex(0);
      this.gui_list.setVisibleRowCount(5);
      this.gui_list.addMouseListener(new java.awt.event.MouseAdapter()
         {
            public void mouseClicked(java.awt.event.MouseEvent evt)
               {
               if (evt.getClickCount() > 1)
                  {
                  goto_selected();
                  }
               }
         });

      javax.swing.JScrollPane list_scroll_pane = new javax.swing.JScrollPane(this.gui_list);
      list_scroll_pane.setPreferredSize(new java.awt.Dimension(200, 100));
      main_panel.add(list_scroll_pane, java.awt.BorderLayout.CENTER);

      // create the south panel
      final javax.swing.JPanel south_panel = new javax.swing.JPanel();
      main_panel.add(south_panel, java.awt.BorderLayout.SOUTH);
      java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
      south_panel.setLayout(gridbag);
      java.awt.GridBagConstraints gridbag_constraints = new java.awt.GridBagConstraints();
      gridbag_constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;

      // create panel to add a new snapshot
      final javax.swing.JPanel add_panel = new javax.swing.JPanel();
      gridbag.setConstraints(add_panel, gridbag_constraints);
      add_panel.setLayout(new java.awt.BorderLayout());
      south_panel.add(add_panel);

      javax.swing.JButton add_button = new javax.swing.JButton(resources.getString("create"));
      AddListener add_listener = new AddListener();
      add_button.addActionListener(add_listener);
      add_panel.add(add_button, java.awt.BorderLayout.WEST);

      this.name_field = new javax.swing.JTextField(10);
      name_field.setText(resources.getString("snapshot") + " 1");
      add_panel.add(name_field, java.awt.BorderLayout.EAST);

      // create delete buttons
      javax.swing.JButton delete_button = new javax.swing.JButton(resources.getString("remove"));
      DeleteListener delete_listener = new DeleteListener();
      delete_button.addActionListener(delete_listener);
      gridbag.setConstraints(delete_button, gridbag_constraints);
      south_panel.add(delete_button);

      javax.swing.JButton delete_all_button = new javax.swing.JButton(resources.getString("remove_all"));
      DeleteAllListener delete_all_listener = new DeleteAllListener();
      delete_all_button.addActionListener(delete_all_listener);
      gridbag.setConstraints(delete_all_button, gridbag_constraints);
      south_panel.add(delete_all_button);

      // create button for the snapshot settings
      javax.swing.JButton settings_button = new javax.swing.JButton(resources.getString("settings"));
      settings_button.setToolTipText(resources.getString("settings_tooltip"));
      SettingsListener settings_listener = new SettingsListener();
      settings_button.addActionListener(settings_listener);
      gridbag.setConstraints(delete_all_button, gridbag_constraints);
      south_panel.add(settings_button);

      p_board_frame.set_context_sensitive_help(this, "WindowSnapshots");

      this.pack();
      }

   public void dispose()
      {
      settings_window.dispose();
      super.dispose();
      }

   @Override
   public void parent_iconified()
      {
      settings_window.parent_iconified();
      super.parent_iconified();
      }

   @Override
   public void parent_deiconified()
      {
      settings_window.parent_deiconified();
      super.parent_deiconified();
      }

   /**
    * Reads the data of this frame from disk. Returns false, if the reading failed.
    */
   @Override
   public boolean read(java.io.ObjectInputStream p_object_stream)
      {
      try
         {
         SnapSavedAttributes saved_attributes = (SnapSavedAttributes) p_object_stream.readObject();
         this.snapshot_count = saved_attributes.snapshot_count;
         this.list_model = saved_attributes.list_model;
         this.gui_list.setModel(this.list_model);
         String next_default_name = "snapshot " + (new Integer(snapshot_count + 1)).toString();
         this.name_field.setText(next_default_name);
         this.setLocation(saved_attributes.location);
         this.setVisible(saved_attributes.is_visible);
         this.settings_window.read(p_object_stream);
         return true;
         }
      catch (Exception e)
         {
         System.out.println("VisibilityFrame.read_attriutes: read failed");
         return false;
         }
      }

   /**
    * Saves this frame to disk.
    */
   @Override
   public void save(java.io.ObjectOutputStream p_object_stream)
      {
      SnapSavedAttributes saved_attributes = new SnapSavedAttributes(this.list_model, this.snapshot_count, this.getLocation(), this.isVisible());
      try
         {
         p_object_stream.writeObject(saved_attributes);
         }
      catch (java.io.IOException e)
         {
         System.out.println("VisibilityFrame.save_attriutes: save failed");
         }
      this.settings_window.save(p_object_stream);
      }

   public void goto_selected()
      {
      int index = gui_list.getSelectedIndex();
      if (index >= 0 && list_model.getSize() > index)
         {
         interactive.IteraBoard board_handling = board_frame.board_panel.board_handling;
         interactive.SnapShot curr_snapshot = (interactive.SnapShot) list_model.elementAt(index);

         curr_snapshot.go_to(board_handling);

         if (curr_snapshot.settings.get_snapshot_attributes().object_colors)
            {
            board_handling.gdi_context.item_color_table = new graphics.ItemColorTableModel(curr_snapshot.graphics_context.item_color_table);
            board_handling.gdi_context.other_color_table = new graphics.OtherColorTableModel(curr_snapshot.graphics_context.other_color_table);

            board_frame.color_manager.set_table_models(board_handling.gdi_context);
            }

         if (curr_snapshot.settings.get_snapshot_attributes().display_region)
            {
            Point viewport_position = curr_snapshot.copy_viewport_position();
            
            if (viewport_position != null)
               {
               board_handling.gdi_context.coordinate_transform = new GdiCoordinateTransform(curr_snapshot.graphics_context.coordinate_transform);
               Dimension panel_size = board_handling.gdi_context.get_panel_size();
               board_frame.board_panel.setSize(panel_size);
               board_frame.board_panel.setPreferredSize(panel_size);
               board_frame.set_viewport_position(viewport_position);
               }
            }

         board_frame.refresh_windows();
         board_frame.hilight_selected_button();
         board_frame.setVisible(true);
         board_frame.repaint();
         }
      }

   /**
    * Refreshs the displayed values in this window.
    */
   public void refresh()
      {
      this.settings_window.refresh();
      }


   private class AddListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         interactive.SnapShot new_snapshot = interactive.SnapShot.get_instance(name_field.getText(), board_frame.board_panel.board_handling);
         if (new_snapshot != null)
            {
            ++snapshot_count;
            list_model.addElement(new_snapshot);
            String next_default_name = resources.getString("snapshot") + " " + (new Integer(snapshot_count + 1)).toString();
            name_field.setText(next_default_name);
            }
         }
      }

   /**
    * Selects the item, which is previous to the current selected item in the list. The current selected item is then no more
    * selected.
    */
   public void select_previous_item()
      {
      if (!this.isVisible())
         {
         return;
         }
      int selected_index = this.gui_list.getSelectedIndex();
      if (selected_index <= 0)
         {
         return;
         }
      this.gui_list.setSelectedIndex(selected_index - 1);
      }

   /**
    * Selects the item, which is next to the current selected item in the list. The current selected item is then no more selected.
    */
   public void select_next_item()
      {
      if (!this.isVisible())
         {
         return;
         }
      int selected_index = this.gui_list.getSelectedIndex();
      if (selected_index < 0 || selected_index >= this.list_model.getSize() - 1)
         {
         return;
         }

      this.gui_list.setSelectedIndex(selected_index + 1);
      }

   private class DeleteListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         SnapShot selected_snapshot = gui_list.getSelectedValue();
         if (selected_snapshot != null)
            {
            list_model.removeElement(selected_snapshot);
            }
         }
      }

   private class DeleteAllListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         list_model.removeAllElements();
         }
      }

   private class GotoListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         goto_selected();
         }
      }

   private class SettingsListener implements java.awt.event.ActionListener
      {
      public void actionPerformed(java.awt.event.ActionEvent p_evt)
         {
         if (first_time)
            {
            java.awt.Point location = getLocation();
            settings_window.setLocation((int) location.getX() + 200, (int) location.getY());
            first_time = false;
            }
         settings_window.setVisible(true);
         }

      boolean first_time = true;
      }

   }