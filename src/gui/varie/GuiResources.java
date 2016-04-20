package gui.varie;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import main.Stat;

public final class GuiResources 
   {
   private final Stat stat;
   
   private ResourceBundle resources;

   public GuiResources ( Stat p_stat, String bundle_key )
      {
      stat = p_stat;
      
      resources = getBundle(bundle_key, stat.locale);
      }

   /**
    * Create a new GuiResource attached to the given bundle_key
    * @param bundle_key
    * @return
    */
   public GuiResources newGuiResources ( String bundle_key )
      {
      return new GuiResources(stat, bundle_key);
      }
   
   private ResourceBundle getBundle (String bundle_key, Locale locale )
      {
      try
         {
         return ResourceBundle.getBundle(bundle_key, locale);
         }
      catch ( Exception exc )
         {
         System.err.println("FrResourceBoundle: missing bundle "+bundle_key);
         return null;
         }
      }
   
   public String getString ( String key )
      {
      if ( key == null ) return "(null)";
      
      try
         {
         return resources.getString(key);
         }
      catch ( Exception exc )
         {
         return key;
         }
      }
   
   public JLabel newJLabel ( String key )
      {
      return new JLabel(getString(key));
      }
   
   public JLabel newJLabel ( String key, String tooltip_en )
      {
      JLabel risul = new JLabel(getString(key));
      
      risul.setToolTipText(getString(tooltip_en));
      
      return risul;
      }

   public JRadioButton newJRadioButton ( String label, ActionListener listener )
      {
      JRadioButton risul = new JRadioButton(getString(label));
      
      if ( listener != null ) risul.addActionListener(listener);
      
      return risul;
      }
   
   public JCheckBox newJCheckBox ( String label, ActionListener listener )
      {
      JCheckBox risul = new JCheckBox(getString(label));
      
      if ( listener != null ) risul.addActionListener(listener);
      
      return risul;
      }
    
   public JCheckBox newJCheckBox ( String label, String tooltip_eng, ActionListener listener )
      {
      JCheckBox risul = new JCheckBox(getString(label));
      
      risul.setToolTipText(getString(tooltip_eng));
      
      if ( listener != null ) risul.addActionListener(listener);
      
      return risul;
      }
   
   
   public JMenuItem newJMenuItem ( String name, String tooltip, ActionListener listener )
      {
      JMenuItem risul = new JMenuItem();
      
      risul.setText(getString(name));
      
      if ( tooltip != null ) risul.setToolTipText(getString(tooltip));
      
      if ( listener != null )
         risul.addActionListener(listener);
      
      return risul;
      }
   
   public JButton newJButton ( String name, ActionListener listener )
      {
      JButton risul = new JButton(getString(name));
      
      if ( listener != null )
         risul.addActionListener(listener);

      return risul;
      }

   public JButton newJButton ( String name, String tooltip, ActionListener listener )
      {
      JButton risul = new JButton(getString(name));

      risul.setToolTipText(getString(tooltip));
      
      if ( listener != null )
         risul.addActionListener(listener);

      return risul;
      }
   
   public JToggleButton newJToggleButton ( String name, String tooltip, ActionListener listener )
      {
      JToggleButton risul = new JToggleButton(getString(name));
   
      risul.setToolTipText(getString(tooltip));
      
      if ( listener != null )
         risul.addActionListener(listener);
      
      return risul;
      }
   
   public TitledBorder newTitledBorder ( String label_en )
      {
      return newTitledBorder(label_en, TitledBorder.RIGHT );
      }
   
   public TitledBorder newTitledBorder ( String label_en, int justification )
      {
      TitledBorder risul = new TitledBorder("  "+getString(label_en)+"  ");
      
      risul.setBorder(new LineBorder(Color.green));
//      risul.setTitleColor(Color.GREEN); this is the text color
      risul.setTitleJustification(justification);
      
      return risul;
      }
   
   }