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
 */

package board.items;

import gui.varie.ObjectInfoPanel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import planar.PlaPoint;
import planar.PlaPointFloat;
import planar.ShapeTile;
import planar.ShapeTileOctagon;
import rules.RuleNets;
import board.BrdConnectable;
import board.RoutingBoard;
import board.algo.AlgoPullTight;
import board.shape.ShapeSearchTree;
import board.shape.ShapeTreeObject;
import board.varie.ItemFixState;
import board.varie.ItemSelectionChoice;
import board.varie.ItemSelectionFilter;

/**
 *
 * Class describing functionality required for traces in the plane.
 *
 * @author Alfons Wirtz
 */

public abstract class BrdTrace extends BrdItem implements BrdConnectable, java.io.Serializable
   {
   private static final long serialVersionUID = 1L;

   private final int trace_half_width; // half width of the trace pen
   private int layer_no; // board layer of the trace
   
   public BrdTrace(int p_layer, int p_half_width, int[] p_net_no_arr, int p_clearance_type, int p_id_no, int p_group_no, ItemFixState p_fixed_state, RoutingBoard p_board)
      {
      super(p_net_no_arr, p_clearance_type, p_id_no, p_group_no, p_fixed_state, p_board);
      
      trace_half_width = p_half_width;

      if ( p_layer <= 0)
         layer_no = 0;
      else if ( p_layer >= p_board.get_layer_count() )
         layer_no = p_board.get_layer_count() - 1;
      else
         layer_no = p_layer;
      }

   /**
    * returns the first corner of the trace
    */
   public abstract PlaPoint first_corner();

   /**
    * returns the last corner of the trace
    */
   public abstract PlaPoint last_corner();

   public int first_layer()
      {
      return layer_no;
      }

   public int last_layer()
      {
      return layer_no;
      }

   public int get_layer()
      {
      return layer_no;
      }

   public void set_layer(int p_layer)
      {
      layer_no = p_layer;
      }

   public final int get_half_width()
      {
      return trace_half_width;
      }

   /**
    * Returns the length of this trace.
    */
   public abstract double get_length();

   /**
    * Returns the half with enlarged by the clearance compensation value for the
    * tree with id number p_ttree_id_no Equals get_half_width(), if no clearance
    * compensation is used in this tree.
    */
   public final int get_compensated_half_width(ShapeSearchTree p_search_tree)
      {
      int result = trace_half_width + p_search_tree.get_clearance_compensation(clearance_class_no(), layer_no);
      return result;
      }

   @Override
   public boolean is_obstacle(BrdItem p_other)
      {
      if (p_other == this || p_other instanceof BrdAreaObstacleVia || p_other instanceof BrdAreaObstacleComp)
         {
         return false;
         }
      if (p_other instanceof BrdAreaConduction && !((BrdAreaConduction) p_other).get_is_obstacle())
         {
         return false;
         }
      if (! p_other.shares_net(this))
         {
         return true;
         }
      return false;
      }

   /**
    * Get a list of all items with a connection point on the layer of this trace
    * equal to its first corner.
    */
   public Set<BrdItem> get_start_contacts()
      {
      return get_normal_contacts(first_corner(), false);
      }

   /**
    * Get a list of all items with a connection point on the layer of this trace
    * equal to its last corner.
    */
   public Set<BrdItem> get_end_contacts()
      {
      return get_normal_contacts(last_corner(), false);
      }

   public PlaPoint normal_contact_point(BrdItem p_other)
      {
      return p_other.normal_contact_point(this);
      }

   public Set<BrdItem> get_normal_contacts()
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();
      PlaPoint start_corner = first_corner();
      if (start_corner != null)
         {
         result.addAll(get_normal_contacts(start_corner, false));
         }
      PlaPoint end_corner = last_corner();
      if (end_corner != null)
         {
         result.addAll(get_normal_contacts(end_corner, false));
         }
      return result;
      }

   @Override
   public boolean is_route()
      {
      return !is_user_fixed() && net_count() > 0;
      }

   /**
    * Returns true, if this trace is not contacted at its first or at its last
    * point.
    */
   public boolean is_tail()
      {
      Collection<BrdItem> contact_list = get_start_contacts();
      if (contact_list.size() == 0)
         {
         return true;
         }
      contact_list = get_end_contacts();
      return (contact_list.size() == 0);
      }

   public java.awt.Color[] get_draw_colors(graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_colors(is_user_fixed());
      }

   public int get_draw_priority()
      {
      return graphics.GdiDrawable.MAX_DRAW_PRIORITY;
      }

   public double get_draw_intensity(graphics.GdiContext p_graphics_context)
      {
      return p_graphics_context.get_trace_color_intensity();
      }

   /**
    * Get a list of all items having a connection point at p_point on the layer of this trace. 
    * If p_ignore_net is false, only contacts to items sharing a
    * net with this trace are calculated. This is the normal case.
    */
   public Set<BrdItem> get_normal_contacts(PlaPoint p_point, boolean p_ignore_net)
      {
      Set<BrdItem> result = new TreeSet<BrdItem>();

      if ( p_point == null ) return result;

      // point should land on either first or last corner
      if ( !(p_point.equals(first_corner()) || p_point.equals(last_corner()))) return result;

      ShapeTile search_shape = p_point.surrounding_box();

      Set<ShapeTreeObject> overlaps = r_board.overlapping_objects(search_shape, layer_no);

      for (ShapeTreeObject curr_ob : overlaps)
         {
         // skip myself
         if ( curr_ob == this ) continue;
         
         if (!(curr_ob instanceof BrdItem)) continue;
  
         BrdItem curr_item = (BrdItem) curr_ob;
         
         // skip items that are on different layers
         if ( ! curr_item.shares_layer(this) ) continue;
         
         // skip there is no net sharing
         if ( ! (p_ignore_net || curr_item.shares_net(this))) continue;
         
         if (curr_item instanceof BrdTrace)
            {
            BrdTrace curr_trace = (BrdTrace) curr_item;
            if (p_point.equals(curr_trace.first_corner()) || p_point.equals(curr_trace.last_corner()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAbit)
            {
            BrdAbit curr_drill_item = (BrdAbit) curr_item;
            if (p_point.equals(curr_drill_item.get_center()))
               {
               result.add(curr_item);
               }
            }
         else if (curr_item instanceof BrdAreaConduction)
            {
            BrdAreaConduction curr_area = (BrdAreaConduction) curr_item;
            if (curr_area.get_area().contains(p_point))
               {
               result.add(curr_item);
               }
            }
         }
      return result;
      }

   public PlaPoint normal_contact_point(BrdAbit p_drill_item)
      {
      return p_drill_item.normal_contact_point(this);
      }

   public PlaPoint normal_contact_point(BrdTrace p_other)
      {
      if ( layer_no != p_other.layer_no) return null;

      boolean contact_at_first_corner = first_corner().equals(p_other.first_corner()) || first_corner().equals(p_other.last_corner());
      boolean contact_at_last_corner = last_corner().equals(p_other.first_corner()) || last_corner().equals(p_other.last_corner());
      PlaPoint result;
      if (!(contact_at_first_corner || contact_at_last_corner) || contact_at_first_corner && contact_at_last_corner)
         {
         // no contact point or more than 1 contact point
         result = null;
         }
      else if (contact_at_first_corner)
         {
         result = first_corner();
         }
      else
         // contact at last corner
         {
         result = last_corner();
         }
      return result;
      }

   @Override
   public boolean is_drillable(int p_net_no)
      {
      return contains_net(p_net_no);
      }

   /**
    * looks, if this trace is connected to the same object at its start and its end point
    */
   public boolean is_overlap()
      {
      Set<BrdItem> start_contacts = get_start_contacts();
      Set<BrdItem> end_contacts = get_end_contacts();
      Iterator<BrdItem> it = end_contacts.iterator();
      while (it.hasNext())
         {
         if (start_contacts.contains(it.next()))
            {
            return true;
            }
         }
      return false;
      }

   /**
    * Returns true, if it is not allowed to change the location of this item by the push algorithm.
    */
   public boolean is_shove_fixed()
      {
      if ( super.is_shove_fixed())  return true;

      // check, if the trace belongs to a net, which is not shovable.
      RuleNets nets = r_board.brd_rules.nets;
      
      for (int curr_net_no : net_no_arr)
         {
         if (rules.RuleNets.is_normal_net_no(curr_net_no))
            {
            if (nets.get(curr_net_no).get_class().is_shove_fixed())
               {
               return true;
               }
            }
         }
      
      return false;
      }

   /**
    * returns the endpoint of this trace with the shortest distance to p_from_point
    */
   public PlaPoint nearest_end_point(PlaPoint p_from_point)
      {
      PlaPoint p1 = first_corner();
      PlaPoint p2 = last_corner();
      PlaPointFloat from_point = p_from_point.to_float();
      double d1 = from_point.distance(p1.to_float());
      double d2 = from_point.distance(p2.to_float());
      PlaPoint result;
      if (d1 < d2)
         {
         result = p1;
         }
      else
         {
         result = p2;
         }
      return result;
      }

   /**
    * Checks, if this trace can be reached by other items via more than one path
    */
   public boolean is_cycle()
      {
      if (is_overlap())
         {
         return true;
         }
      Set<BrdItem> visited_items = new TreeSet<BrdItem>();
      Collection<BrdItem> start_contacts = get_start_contacts();
      // a cycle exists if through expanding the start contact we reach
      // this trace again via an end contact
      for (BrdItem curr_contact : start_contacts)
         {
         // make shure, that all direct neighbours are
         // expanded from here, to block coming back to
         // this trace via a start contact.
         visited_items.add(curr_contact);
         }
      boolean ignore_areas = false;
      if (net_no_arr.length > 0)
         {
         rules.RuleNet curr_net = r_board.brd_rules.nets.get(net_no_arr[0]);
         if (curr_net != null && curr_net.get_class() != null)
            {
            ignore_areas = curr_net.get_class().get_ignore_cycles_with_areas();
            }
         }
      for (BrdItem curr_contact : start_contacts)
         {
         if (curr_contact.is_cycle_recu(visited_items, this, this, ignore_areas))
            {
            return true;
            }
         }
      return false;
      }

   @Override
   public int shape_layer(int p_index)
      {
      return layer_no;
      }

   @Override
   public PlaPoint[] get_ratsnest_corners()
      {
      // Use only uncontacted enpoints of the trace.
      // Otherwise the allocated memory in the calculation of the incompletes
      // might become very big.
      int stub_count = 0;
      boolean stub_at_start = false;
      boolean stub_at_end = false;
      if (get_start_contacts().isEmpty())
         {
         ++stub_count;
         stub_at_start = true;
         }
      if (get_end_contacts().isEmpty())
         {
         ++stub_count;
         stub_at_end = true;
         }
      PlaPoint[] result = new PlaPoint[stub_count];
      int stub_no = 0;
      if (stub_at_start)
         {
         result[stub_no] = first_corner();
         ++stub_no;
         }
      if (stub_at_end)
         {
         result[stub_no] = last_corner();
         }
      for (int i = 0; i < result.length; ++i)
         {
         if (result[i] == null)
            {
            return new PlaPoint[0];// Trace is inconsistent
            }
         }
      return result;
      }

   /**
    * checks, that the connection restrictions to the contact pins are
    * satisfied. If p_at_start, the start of this trace is checked, else the
    * end. Returns false, if a pin is at that end, where the connection is
    * checked and the connection is not ok.
    */
   public abstract boolean check_connection_to_pin(boolean p_at_start);

   public boolean is_selected_by_filter(ItemSelectionFilter p_filter)
      {
      if (!is_selected_by_fixed_filter(p_filter))
         {
         return false;
         }
      return p_filter.is_selected(ItemSelectionChoice.TRACES);
      }

   /**
    * Looks up touching pins at the first corner and the last corner of the trace. 
    * Used to avoid acid traps.
    */
   public final Set<BrdAbitPin> touching_pins_at_end_corners()
      {
      Set<BrdAbitPin> result = new TreeSet<BrdAbitPin>();

      PlaPoint curr_end_point = first_corner();
      for (int i = 0; i < 2; ++i)
         {
         ShapeTileOctagon curr_oct = curr_end_point.surrounding_octagon();
         curr_oct = curr_oct.enlarge(trace_half_width);
         Set<BrdItem> curr_overlaps = r_board.overlapping_items_with_clearance(curr_oct, layer_no, new int[0], clearance_class_no());
         for (BrdItem curr_item : curr_overlaps)
            {
            if ((curr_item instanceof BrdAbitPin) && curr_item.shares_net(this))
               {
               result.add((BrdAbitPin) curr_item);
               }
            }
         curr_end_point = last_corner();
         }
      return result;
      }

   public void print_info(ObjectInfoPanel p_window, java.util.Locale p_locale)
      {
      java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("board.resources.ObjectInfoPanel", p_locale);
      p_window.append_bold(resources.getString("trace"));
      p_window.append(" " + resources.getString("from"));
      p_window.append(first_corner().to_float());
      p_window.append(resources.getString("to"));
      p_window.append(last_corner().to_float());
      p_window.append(resources.getString("on_layer") + " ");
      p_window.append(r_board.layer_structure.get_name(layer_no));
      p_window.append(", " + resources.getString("width") + " ");
      p_window.append(2 * trace_half_width);
      p_window.append(", " + resources.getString("length") + " ");
      p_window.append(get_length());
      print_connectable_item_info(p_window, p_locale);
      p_window.newline();
      }

   @Override
   public boolean validate_ok()
      {
      boolean result = super.validate_ok();

      if (first_corner().equals( last_corner()))
         {
         System.out.println("Trace.validate: first and last corner are equal");
         result = false;
         }
      
      return result;
      }

   /**
    * looks, if this trace can be combined with other traces
    * @return true, if Something has been combined.
    */
   public abstract boolean combine();

   /**
    * Looks up traces intersecting with this trace and splits them at the
    * intersection points. In case of an overlaps, the traces are split at their
    * first and their last common point. Returns the pieces resulting from
    * splitting. If nothing is split, the result will contain just this Trace.
    * If p_clip_shape != null, the split may be resticted to p_clip_shape.
    */
   public abstract Collection<BrdTracePolyline> split(ShapeTileOctagon p_clip_shape);

   /**
    * Splits this trace into two at p_point. 
    * can return null i for example p_point is not located on this trace.
    * This method does NOT change the trace, it returns pieces
    * @return the 2 pieces of the split trace, or null if nothing was split 
    */
   public abstract BrdTrace[] split(PlaPoint p_point);

   /**
    * Tries to make this trace shorter according to its rules. Returns true if
    * the geometry of the trace was changed.
    */
   public abstract boolean pull_tight(AlgoPullTight p_pull_tight_algo);
   }