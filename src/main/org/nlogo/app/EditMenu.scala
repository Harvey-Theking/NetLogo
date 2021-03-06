// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/** note that multiple instances of this class may exist as there are now multiple frames that each
 have their own menu bar and menus ev 8/25/05 */

import org.nlogo.editor.Actions
import org.nlogo.api.I18N

class EditMenu(app: App) extends org.nlogo.swing.Menu(I18N.gui.get("menu.edit"))
with Events.SwitchedTabsEvent.Handler
with org.nlogo.window.Events.LoadSectionEvent.Handler
{

  implicit val i18nName = I18N.Prefix("menu.edit")

  val snapAction = new javax.swing.AbstractAction(I18N.gui("snapToGrid")) {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      app.workspace.snapOn(!app.workspace.snapOn)
    }}

  private var snapper: javax.swing.JCheckBoxMenuItem = null

  //TODO i18n - do we need to change the shortcut keys too?
  addMenuItem('Z', org.nlogo.editor.UndoManager.undoAction)
  addMenuItem('Y', org.nlogo.editor.UndoManager.redoAction)
  addSeparator()
  addMenuItem(I18N.gui("cut"), 'X', Actions.CUT_ACTION )
  addMenuItem(I18N.gui("copy"), 'C', Actions.COPY_ACTION)
  addMenuItem(I18N.gui("paste"), 'V', Actions.PASTE_ACTION)
  addMenuItem(I18N.gui("delete"), (java.awt.event.KeyEvent.VK_DELETE).toChar, Actions.DELETE_ACTION, false)
  addSeparator()
  addMenuItem(I18N.gui("selectAll"), 'A', Actions.SELECT_ALL_ACTION)
  addSeparator()
  addMenuItem(I18N.gui("find"), 'F', org.nlogo.app.FindDialog.FIND_ACTION)
  addMenuItem(I18N.gui("findNext"), 'G', org.nlogo.app.FindDialog.FIND_NEXT_ACTION)
  addSeparator()
  addMenuItem(I18N.gui("shiftLeft"), '[', org.nlogo.editor.Actions.shiftLeftAction)
  addMenuItem(I18N.gui("shiftRight"), ']', org.nlogo.editor.Actions.shiftRightAction)
  addMenuItem(I18N.gui("format"), (java.awt.event.KeyEvent.VK_TAB).toChar, org.nlogo.editor.Actions.tabKeyAction, false)
  addSeparator()
  addMenuItem(I18N.gui("comment") + " / " + I18N.gui("uncomment"), ';', org.nlogo.editor.Actions.commentToggleAction)
  addSeparator()
  snapper = addCheckBoxMenuItem(I18N.gui("snapToGrid"), app.workspace.snapOn(), snapAction)

  addMenuListener(new javax.swing.event.MenuListener() {
    override def menuSelected(e: javax.swing.event.MenuEvent): Unit = {
      Actions.CUT_ACTION.setEnabled(app.tabs.proceduresTab.isTextSelected())
      Actions.COPY_ACTION.setEnabled(app.tabs.proceduresTab.isTextSelected())
      Actions.PASTE_ACTION.setEnabled(java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
        .isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.stringFlavor))
    }

    override def menuDeselected(e: javax.swing.event.MenuEvent): Unit = {
    }

    override def menuCanceled(e: javax.swing.event.MenuEvent): Unit = {
    }
  })

  def handle(e: Events.SwitchedTabsEvent) {
    snapAction.setEnabled(e.newTab == app.tabs.interfaceTab)
  }

  def handle(e: org.nlogo.window.Events.LoadSectionEvent) {
    if(e.section == org.nlogo.api.ModelSection.ModelSettings) {
      app.workspace.snapOn(e.lines != null &&
                           e.lines.nonEmpty &&
                           e.lines.head.trim.nonEmpty &&
                           e.lines.head.toInt != 0)
      snapper.setState(app.workspace.snapOn)
    }
  }
}
