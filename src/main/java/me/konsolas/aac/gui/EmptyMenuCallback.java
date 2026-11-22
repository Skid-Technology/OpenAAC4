package me.konsolas.aac.gui;

public class EmptyMenuCallback implements MenuCallback<MenuClick> {
   public final ContextClickMenuItem contextClickMenuItemValue;

   public EmptyMenuCallback(ContextClickMenuItem contextClickMenuItem) {
      this.contextClickMenuItemValue = contextClickMenuItem;
   }

   @Override
   public void accept(MenuClick menuClick) {
   }
}
