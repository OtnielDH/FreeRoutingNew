package autoroute.varie;

import planar.PlaPointFloat;
import board.items.BrdAbitPin;

public final class ArtPin implements Comparable<ArtPin>
   {
   public final BrdAbitPin board_pin;
   final double distance_to_component_center;

   ArtPin(BrdAbitPin p_board_pin, PlaPointFloat gravity_center_of_smd_pins)
      {
      this.board_pin = p_board_pin;
      PlaPointFloat pin_location = p_board_pin.get_center().to_float();
      distance_to_component_center = pin_location.distance(gravity_center_of_smd_pins);
      }

   public int compareTo(ArtPin p_other)
      {
      int result;
      double delta_dist = this.distance_to_component_center - p_other.distance_to_component_center;
      if (delta_dist > 0)
         {
         result = 1;
         }
      else if (delta_dist < 0)
         {
         result = -1;
         }
      else
         {
         result = this.board_pin.pin_no - p_other.board_pin.pin_no;
         }
      return result;
      }
   }