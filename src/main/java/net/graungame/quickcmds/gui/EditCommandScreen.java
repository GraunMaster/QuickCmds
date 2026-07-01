package net.graungame.quickcmds.gui;

import net.graungame.quickcmds.config.QuickCommand;
import net.graungame.quickcmds.config.QuickCmdsConfig;
import net.graungame.quickcmds.keybind.KeybindManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EditCommandScreen extends Screen {

    private static final int C_BG_SECTION  = 0x99000000;
    private static final int C_BG_ROW_HOV  = 0xCC000000;
    private static final int C_BG_BTN      = 0xCC111118;
    private static final int C_BG_BTN_HOV  = 0xCC1A1A28;
    private static final int C_BG_FIELD    = 0xBB080810;
    private static final int C_BG_DROPDOWN = 0xEE0D0D14;
    private static final int C_CARD_BG     = 0x99000000;
    private static final int C_TAB_UL      = 0xFF5A5ACC;
    private static final int C_TEXT        = 0xFFDDDDDD;
    private static final int C_TEXT_DIM    = 0xFF888898;
    private static final int C_TEXT_HINT   = 0xFF444455;
    private static final int C_TEXT_ERR    = 0xFFCC3333;
    private static final int SCROLL_BAR_W  = 5;
    private static final int MAX_CONTENT_W = 480;
    private static final int SECTION_GAP   = 12;

    private int TAB_H, ROW_H, BOTTOM_BAR_H, PAD, FIELD_H, LABEL_GAP;

    private float uiScale() {
        float byWidth  = width  >= 400 ? 1.0f : Math.max(0.45f, width  / 400f);
        float byHeight = height >= 300 ? 1.0f : Math.max(0.45f, height / 300f);
        return Math.min(byWidth, byHeight);
    }
    private void calcLayout() {
        float s  = uiScale();
        
        boolean compact = height < 300;
        TAB_H        = Math.round((compact ? 18 : 22) * s);
        ROW_H        = Math.round((compact ? 17 : 21) * s);
        BOTTOM_BAR_H = Math.round((compact ? 24 : 30) * s);
        PAD          = Math.max(2, Math.round((compact ? 4 : 6) * s));
        FIELD_H      = Math.round((compact ? 13 : 15) * s);
        
        LABEL_GAP    = Math.max(PAD*2, 12);
    }

    private String[] cachedTypeLabels;
    private String[] cachedTabLabels;
    private Text[]   cachedTabText;
    private Text[]   cachedTypeLabelText;
    private Text tSectionGeneral, tSectionSizes;
    private Text tLabelScale, tLabelKeyPanel, tLabelKeySettings;
    private Text tLabelOuterR, tLabelInnerR, tLabelDeadR;
    private Text tPressKey;
    private Text tAppColor, tAppAlpha, tAppAlphaNorm, tAppAlphaHov, tAppTextColor, tAppIconScale;
    private Text tBtnSave, tBtnDelete, tBtnCancel, tBtnApply, tBtnDone, tBtnAdd;
    private Text tSlotNew, tSlotEdit;
    private Text tFieldLabelShort, tFieldCommand, tFieldText;
    private Text tHotkeyAssign, tHotkeyNone, tHotkeyEscCancel;
    private Text tErrorFill, tErrorFillLabel;
    private Text tTooltipHint, tTooltipType, tTooltipDelay;
    private Text tFieldActions, tFieldDelay, tBtnIcon, tIconNone;

    private String cachedScaleStr = ""; private float cachedScaleVal = -1f;
    private String cachedOuterStr = ""; private int   cachedOuterVal = -1;
    private String cachedInnerStr = ""; private int   cachedInnerVal = -1;
    private String cachedDeadStr  = ""; private int   cachedDeadVal  = -1;
    private String cachedAlphaNormStr = ""; private int cachedAlphaNormVal = -1;
    private String cachedAlphaHovStr  = ""; private int cachedAlphaHovVal  = -1;
    private String cachedIconScaleStr = ""; private float cachedIconScaleVal = -1f;

    private void rebuildTextCache() {
        cachedTypeLabels = new String[]{
            I18n.translate("quickcmds.type.command"),
            I18n.translate("quickcmds.type.chat"),
            I18n.translate("quickcmds.type.hotkey")
        };
        cachedTabLabels = new String[]{
            I18n.translate("quickcmds.tab.commands"),
            I18n.translate("quickcmds.tab.settings"),
            I18n.translate("quickcmds.tab.appearance")
        };
        cachedTabText = new Text[]{ Text.literal(cachedTabLabels[0]), Text.literal(cachedTabLabels[1]), Text.literal(cachedTabLabels[2]) };
        cachedTypeLabelText = new Text[]{ Text.literal(cachedTypeLabels[0]), Text.literal(cachedTypeLabels[1]), Text.literal(cachedTypeLabels[2]) };
        tSectionGeneral   = Text.translatable("quickcmds.settings.section_general");
        tSectionSizes     = Text.translatable("quickcmds.settings.section_sizes");
        tLabelScale       = Text.literal(I18n.translate("quickcmds.settings.scale"));
        tLabelKeyPanel    = Text.literal(I18n.translate("quickcmds.settings.key_panel"));
        tLabelKeySettings = Text.literal(I18n.translate("quickcmds.settings.key_settings"));
        tLabelOuterR      = Text.literal(I18n.translate("quickcmds.settings.outer_r"));
        tLabelInnerR      = Text.literal(I18n.translate("quickcmds.settings.inner_r"));
        tLabelDeadR       = Text.literal(I18n.translate("quickcmds.settings.dead_r"));
        tPressKey         = Text.literal(I18n.translate("quickcmds.settings.press_key"));
        tAppColor         = Text.translatable("quickcmds.appearance.color");
        tAppAlpha         = Text.translatable("quickcmds.appearance.alpha");
        tAppAlphaNorm     = Text.translatable("quickcmds.appearance.alpha_normal");
        tAppAlphaHov      = Text.translatable("quickcmds.appearance.alpha_hover");
        tAppTextColor     = Text.translatable("quickcmds.appearance.text_color");
        tAppIconScale     = Text.translatable("quickcmds.appearance.icon_scale");
        tBtnSave     = Text.literal(I18n.translate("quickcmds.btn.save"));
        tBtnDelete   = Text.literal(I18n.translate("quickcmds.btn.delete"));
        tBtnCancel   = Text.literal(I18n.translate("quickcmds.btn.cancel"));
        tBtnApply    = Text.literal(I18n.translate("quickcmds.btn.apply"));
        tBtnDone     = Text.literal(I18n.translate("quickcmds.btn.done"));
        tBtnAdd      = Text.literal(I18n.translate("quickcmds.btn.add"));
        tSlotNew     = Text.literal(I18n.translate("quickcmds.slot.new"));
        tSlotEdit    = Text.literal(I18n.translate("quickcmds.slot.edit", slotIndex + 1));
        tFieldLabelShort = Text.translatable("quickcmds.field.label_short");
        tFieldCommand    = Text.literal(I18n.translate("quickcmds.field.command"));
        tFieldText       = Text.literal(I18n.translate("quickcmds.field.text"));
        tHotkeyAssign    = Text.translatable("quickcmds.hotkey.assign");
        tHotkeyNone      = Text.literal(I18n.translate("quickcmds.hotkey.none"));
        tHotkeyEscCancel = Text.translatable("quickcmds.hotkey.esc_cancel");
        tErrorFill       = Text.literal(I18n.translate("quickcmds.error.fill"));
        tErrorFillLabel  = Text.translatable("quickcmds.error.fill_label");
        tTooltipHint     = Text.literal(I18n.translate("quickcmds.tooltip.hint"));
        tTooltipType     = Text.literal(I18n.translate("quickcmds.tooltip.type") + " ");
        tTooltipDelay    = Text.literal(I18n.translate("quickcmds.tooltip.delay"));
        tFieldActions    = Text.translatable("quickcmds.field.actions");
        tFieldDelay      = Text.translatable("quickcmds.field.delay");
        tBtnIcon         = Text.translatable("quickcmds.btn.icon");
        tIconNone        = Text.translatable("quickcmds.icon.none");
        cachedScaleVal = -1f;
        cachedOuterVal = cachedInnerVal = cachedDeadVal = -1;
        cachedAlphaNormVal = cachedAlphaHovVal = -1;
    }

    private String scaleStr()     { float v=QuickCmdsConfig.getMenuScale();      if(v!=cachedScaleVal){cachedScaleVal=v;cachedScaleStr=String.format("%.1f×",v);} return cachedScaleStr; }
    private String outerStr()     { int v=QuickCmdsConfig.getOuterRadius();      if(v!=cachedOuterVal){cachedOuterVal=v;cachedOuterStr=v+"px";}   return cachedOuterStr; }
    private String innerStr()     { int v=QuickCmdsConfig.getInnerRadius();      if(v!=cachedInnerVal){cachedInnerVal=v;cachedInnerStr=v+"px";}   return cachedInnerStr; }
    private String deadStr()      { int v=QuickCmdsConfig.getDeadRadius();       if(v!=cachedDeadVal) {cachedDeadVal=v;cachedDeadStr=v+"px";}    return cachedDeadStr; }
    private String alphaNormStr() { int v=QuickCmdsConfig.getSegAlpha();   if(v!=cachedAlphaNormVal){cachedAlphaNormVal=v;int p=(int)((v-QuickCmdsConfig.ALPHA_MIN)*100f/(QuickCmdsConfig.ALPHA_MAX-QuickCmdsConfig.ALPHA_MIN));cachedAlphaNormStr=p+"%";}return cachedAlphaNormStr;}
    private String alphaHovStr()  { int v=QuickCmdsConfig.getSegAlphaHov();if(v!=cachedAlphaHovVal) {cachedAlphaHovVal=v;int p=(int)((v-QuickCmdsConfig.ALPHA_MIN)*100f/(QuickCmdsConfig.ALPHA_MAX-QuickCmdsConfig.ALPHA_MIN));cachedAlphaHovStr=p+"%"; }return cachedAlphaHovStr;}
    private String iconScaleStr() { float v=QuickCmdsConfig.getIconScale(); if(v!=cachedIconScaleVal){cachedIconScaleVal=v;cachedIconScaleStr=String.format("%.1f×",v);} return cachedIconScaleStr; }
    private String keyPanelStr()    { return KeybindManager.OPEN_PANEL.getBoundKeyLocalizedText().getString(); }
    private String keySettingsStr() { return KeybindManager.OPEN_SETTINGS.getBoundKeyLocalizedText().getString(); }
    private String[] typeLabels()   { return cachedTypeLabels!=null?cachedTypeLabels:new String[]{"Команда","Чат","Хоткей"}; }
    private String[] tabLabels()    { return cachedTabLabels!=null?cachedTabLabels:new String[]{"Команды","Настройки","Вид"}; }

    private final Screen parent;
    private final int    slotIndex;
    private final boolean isCommandEdit;
    private final QuickCommand existing;

    private int  activeTab    = 0;
    private int  actionType   = 0;
    private int  scrollOffset = 0;
    private int  hoverRow     = -1;

    private MultilineTextFieldWidget labelField;
    
    private MultilineTextFieldWidget actionsField;
    
    private int textareaRows() { return height < 300 ? 2 : (height < 400 ? 3 : 6); }

    private float editDelay = 0f;
    private Rect  rDelaySlider;
    private boolean delayDragging = false;
    private String cachedDelayStr = "";

    private String editIcon = "";
    
    private String savedLabel   = null;
    private String savedActions = null;
    private Rect   rBtnIcon;
    private Rect   rIconPreview;

    private boolean capturingSlotKey = false;
    private boolean capturingPanel   = false;
    private boolean capturingSettings= false;
    private final List<String> capturedCombo = new ArrayList<>();
    private String currentKeybind = "";

    private boolean labelError  = false;
    private boolean actionError = false;
    private boolean dropdownOpen = false;

    private double touchScrollAnchorY = 0;
    private int    touchScrollAnchorOffset = 0;
    private boolean touchScrolling = false;
    private static final int TOUCH_SCROLL_THRESHOLD = 6;
    private boolean scrollBarDragging = false;
    private double  scrollBarAnchorY  = 0;
    private int     scrollBarAnchorOffset = 0;

    private int listX, listY, listW, listH, bottomY;

    private List<Text> pendingTooltip = null;
    private int tooltipMouseX, tooltipMouseY;
    private int hoverTickX=-1, hoverTickY=-1, hoverTicks=0;
    private static final int TOOLTIP_DELAY_TICKS = 20;

    private boolean hasUnsavedChanges = false;

    private record Rect(int x,int y,int w,int h) { boolean contains(int mx,int my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;} }

    private final List<Rect> rTabs=new ArrayList<>(), rRows=new ArrayList<>(), rDropItems=new ArrayList<>();
    private Rect rDropTrig, rBtnAssign, rBtnApply, rBtnDone, rBtnDel;
    private Rect rSlider, rSliderHov;
    private Rect rSliderOuter, rSliderInner, rSliderDead;
    private Rect rBtnApplySettings, rBtnKeyPanel, rBtnKeySettings, rBtnAdd;

    private boolean sliderDragging=false, sliderHovDragging=false;
    private boolean sliderOuterDragging=false, sliderInnerDragging=false, sliderDeadDragging=false;

    private MultilineTextFieldWidget hexField, hexTxtHovField;
    private Rect rColorPreview, rTxtHovColorPreview, rIconScaleSlider;
    private boolean iconScaleDragging = false;
    private String prevHexValue = "";

    public EditCommandScreen(Screen parent) {
        super(Text.literal("QuickCmds"));
        this.parent=parent; this.slotIndex=Integer.MIN_VALUE; this.isCommandEdit=false; this.existing=null;
    }
    public EditCommandScreen(Screen parent, int slotIndex) {
        super(Text.literal("QuickCmds"));
        this.parent=parent; this.slotIndex=slotIndex;
        
        this.isCommandEdit=(slotIndex!=Integer.MIN_VALUE && slotIndex!=-2);
        if (slotIndex==-2) this.activeTab=1;
        this.existing=(slotIndex>=0&&slotIndex<QuickCmdsConfig.getCommands().size())
                ?QuickCmdsConfig.getCommands().get(slotIndex):null;
        if (existing!=null) {
            actionType=switch(existing.type==null?"":existing.type){case"chat"->1;case"hotkey"->2;default->0;};
            currentKeybind=existing.keybind!=null?existing.keybind:"";
            if (actionType==2&&existing.action!=null) currentKeybind=existing.action;
            editDelay=Math.max(QuickCmdsConfig.DELAY_MIN,Math.min(QuickCmdsConfig.DELAY_MAX,existing.delay));
            editIcon=existing.icon!=null?existing.icon:"";
        }
    }
    public EditCommandScreen(Screen parent, int slotIndex, int tab) {
        this(parent, slotIndex==Integer.MIN_VALUE?Integer.MIN_VALUE:slotIndex);
        this.activeTab=(tab==-2)?1:0;
    }

    @Override protected void init() {
        rebuildTextCache(); calcLayout(); clearChildren();
        rTabs.clear(); rRows.clear(); rDropItems.clear();
        rDropTrig=rBtnAssign=rBtnApply=rBtnDone=rBtnDel=null;
        rBtnKeyPanel=rBtnKeySettings=rBtnAdd=null; rBtnApplySettings=null;
        rSlider=rSliderHov=null; rColorPreview=null;
        hexField=hexTxtHovField=null;
        rTxtHovColorPreview=null; rIconScaleSlider=null; iconScaleDragging=false;
        rSliderOuter=rSliderInner=rSliderDead=null;
        sliderOuterDragging=sliderInnerDragging=sliderDeadDragging=false;
        labelField=null; actionsField=null;
        rDelaySlider=null; rBtnIcon=null; rIconPreview=null;

        bottomY=height-BOTTOM_BAR_H;
        int contentY=TAB_H, contentH=bottomY-contentY;
        int sidePad=Math.max(2,PAD);
        int w=Math.min(width-sidePad*2,MAX_CONTENT_W);
        listX=(width-w)/2; listY=contentY; listW=w; listH=contentH;

        if (isCommandEdit) {
            setupCommandEdit();
        } else {
            int tabGap=2, tabW=(listW-tabGap*2)/3;
            for (int i=0;i<3;i++) rTabs.add(new Rect(listX+i*(tabW+tabGap),0,tabW,TAB_H));
            if      (activeTab==0) setupTabCommands();
            else if (activeTab==1) setupTabSettings();
            else                   setupTabView();
        }

        int btnH=Math.round(20*uiScale());
        if (isCommandEdit) {
            int bw=Math.min(listW/2-PAD,Math.round(90*uiScale()));
            int by=bottomY+(BOTTOM_BAR_H-btnH)/2;
            rBtnApply=new Rect(listX,by,bw,btnH);
            rBtnDel  =new Rect(listX+bw+PAD,by,bw,btnH);
        } else {
            int bw=Math.min(listW/3-PAD,Math.round(80*uiScale()));
            int by=bottomY+(BOTTOM_BAR_H-btnH)/2;
            rBtnDone=new Rect(listX+listW-bw,by,bw,btnH);
            if (activeTab==0&&QuickCmdsConfig.canAdd())
                rBtnAdd=new Rect(listX+listW-bw*2-PAD,by,bw,btnH);
            if (activeTab==1||activeTab==2)
                rBtnApplySettings=new Rect(listX+listW-bw*2-PAD,by,bw,btnH);
        }
    }

    private void setupTabCommands() {
        var cmds=QuickCmdsConfig.getCommands();
        int y=listY+4, rowW=listW-SCROLL_BAR_W-2;
        int visH=listH-ROW_H-8, vis=Math.max(1,visH/(ROW_H+1));
        scrollOffset=Math.max(0,Math.min(scrollOffset,Math.max(0,cmds.size()-vis)));
        for (int vi=0;vi<vis;vi++) {
            int i=vi+scrollOffset; if(i>=cmds.size())break;
            rRows.add(new Rect(listX,y+vi*(ROW_H+1),rowW,ROW_H));
        }
    }

    private void setupTabSettings() {
        int y=listY+LABEL_GAP+10; rRows.clear();
        rRows.add(new Rect(listX,y,listW,ROW_H));
        rRows.add(new Rect(listX,y+ROW_H+1,listW,ROW_H));
        rRows.add(new Rect(listX,y+(ROW_H+1)*2,listW,ROW_H));
        rBtnKeyPanel=rRows.get(1); rBtnKeySettings=rRows.get(2);
        int afterKeys=y+(ROW_H+1)*3+SECTION_GAP+10;
        rSliderOuter=new Rect(listX,afterKeys,listW,ROW_H);
        rSliderInner=new Rect(listX,afterKeys+ROW_H+1,listW,ROW_H);
        rSliderDead =new Rect(listX,afterKeys+(ROW_H+1)*2,listW,ROW_H);
    }

    private void setupTabView() {
        int y=listY+LABEL_GAP+4; int previewSize=ROW_H; int hexW=listW-previewSize-PAD;
        
        hexField=addHexField(listX,y,hexW,FIELD_H,colorToHex(QuickCmdsConfig.getSegColorR(),QuickCmdsConfig.getSegColorG(),QuickCmdsConfig.getSegColorB()));
        rColorPreview=new Rect(listX+hexW+PAD,y,previewSize,FIELD_H);
        y+=FIELD_H+SECTION_GAP;
        
        rRows.clear();
        rRows.add(new Rect(listX,y,listW,ROW_H)); rSlider=rRows.get(0);
        rRows.add(new Rect(listX,y+ROW_H+1,listW,ROW_H)); rSliderHov=rRows.get(1);
        y+=ROW_H*2+1+SECTION_GAP+LABEL_GAP;
        
        hexTxtHovField=addHexField(listX,y,hexW,FIELD_H,colorToHex(QuickCmdsConfig.getTextHovR(),QuickCmdsConfig.getTextHovG(),QuickCmdsConfig.getTextHovB()));
        rTxtHovColorPreview=new Rect(listX+hexW+PAD,y,previewSize,FIELD_H);
        y+=FIELD_H+SECTION_GAP+LABEL_GAP;
        
        rIconScaleSlider=new Rect(listX,y,listW,ROW_H);
    }

    private void setupCommandEdit() {
        int y=listY+PAD*2;
        
        rDropTrig=new Rect(listX,y,listW,ROW_H); y+=ROW_H+LABEL_GAP;

        if (actionType==2) {
            
            labelField=addField(listX,y,listW,FIELD_H,I18n.translate("quickcmds.field.label"),existing!=null?existing.label:null);
            y+=FIELD_H+PAD*2;
            rBtnAssign=new Rect(listX,y,listW,ROW_H);
            y+=ROW_H+PAD;
            
            y+=ROW_H+PAD*2;

            int iconBtnW=listW-ROW_H-PAD;
            rBtnIcon=new Rect(listX,y,iconBtnW,ROW_H);
            rIconPreview=new Rect(listX+iconBtnW+PAD,y,ROW_H,ROW_H);
        } else {
            
            String initLabel   = savedLabel   != null ? savedLabel   : (existing != null ? existing.label           : null);
            String initActions = savedActions != null ? savedActions : (existing != null ? existing.getActionsText() : null);
            labelField=addField(listX,y,listW,FIELD_H,I18n.translate("quickcmds.field.label"), initLabel);
            y+=FIELD_H+LABEL_GAP;

            int taH=FIELD_H*textareaRows()+FIELD_H;
            actionsField=new MultilineTextFieldWidget(textRenderer,listX,y,listW,Math.max(FIELD_H,taH),Text.literal(""));
            actionsField.setMaxLength(4096);
            actionsField.setPlaceholder(Text.literal(actionType==1
                ? I18n.translate("quickcmds.placeholder.chat")
                : I18n.translate("quickcmds.placeholder.command")));
            actionsField.setLineHeight(Math.max(9, FIELD_H));
            if (initActions != null && !initActions.isBlank()) {
                actionsField.setText(initActions);
            }
            addDrawableChild(actionsField);
            y+=Math.max(FIELD_H,taH)+PAD*2;

            rDelaySlider=new Rect(listX,y,listW,ROW_H);
            y+=ROW_H+PAD*2;

            int iconBtnW=listW-ROW_H-PAD;
            rBtnIcon=new Rect(listX,y,iconBtnW,ROW_H);
            rIconPreview=new Rect(listX+iconBtnW+PAD,y,ROW_H,ROW_H);
        }
    }

    private MultilineTextFieldWidget addField(int x,int y,int w,int h,String ph,String val) {
        var f=new MultilineTextFieldWidget(textRenderer,x,y,w,h,Text.literal(""));
        f.setMaxLength(256); f.setPlaceholder(Text.literal(ph)); f.setSingleLine(true);
        if (val!=null&&!val.isBlank()) f.setText(val);
        addDrawableChild(f); return f;
    }

    private MultilineTextFieldWidget addHexField(int x,int y,int w,int h,String val) {
        var f=new MultilineTextFieldWidget(textRenderer,x,y,w,h,Text.literal(""));
        f.setMaxLength(7); f.setPlaceholder(Text.literal(I18n.translate("quickcmds.appearance.color_hint"))); f.setSingleLine(true);
        if (val!=null&&!val.isBlank()) f.setText(val);
        addDrawableChild(f); return f;
    }

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        if(mx==hoverTickX&&my==hoverTickY){hoverTicks++;}else{hoverTicks=0;hoverTickX=mx;hoverTickY=my;pendingTooltip=null;}

        if (isCommandEdit) renderCommandEdit(ctx,mx,my);
        else { renderTabs(ctx,mx,my); renderListPanel(ctx,mx,my); }

        renderBottomBar(ctx,mx,my);
        super.render(ctx,mx,my,delta);
        if (isCommandEdit&&dropdownOpen) renderDropdown(ctx,mx,my);

        if (pendingTooltip!=null&&hoverTicks>=TOOLTIP_DELAY_TICKS)
            ctx.drawTooltip(textRenderer,pendingTooltip,tooltipMouseX,tooltipMouseY);
    }

    private void renderTabs(DrawContext ctx, int mx, int my) {
        for (int i=0;i<rTabs.size();i++) {
            var r=rTabs.get(i); boolean active=(activeTab==i), hov=r.contains(mx,my)&&!active;
            ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),active?0xDD000000:(hov?0xBB111122:C_BG_SECTION));
            if (active) ctx.fill(r.x()+4,r.y()+r.h()-2,r.x()+r.w()-4,r.y()+r.h(),C_TAB_UL);
            ctx.drawCenteredTextWithShadow(textRenderer,cachedTabText[i],r.x()+r.w()/2,r.y()+(r.h()-8)/2,active?C_TEXT:(hov?0xFFCCCCCC:C_TEXT_DIM));
        }
    }

    private void renderListPanel(DrawContext ctx, int mx, int my) {
        if      (activeTab==0) renderRowsCommands(ctx,mx,my);
        else if (activeTab==1) renderRowsSettings(ctx,mx,my);
        else                   renderTabView(ctx,mx,my);
    }

    private void renderRowsCommands(DrawContext ctx, int mx, int my) {
        var cmds=QuickCmdsConfig.getCommands(); hoverRow=-1;
        for (int vi=0;vi<rRows.size();vi++) {
            int idx=vi+scrollOffset; if(idx>=cmds.size())break;
            var r=rRows.get(vi); var cmd=cmds.get(idx); boolean hov=r.contains(mx,my);
            if (hov){ hoverRow=vi; pendingTooltip=buildCmdTooltip(cmd); tooltipMouseX=mx; tooltipMouseY=my; }
            ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_ROW_HOV:C_BG_SECTION);

            int textOffX=PAD;
            String icon=cmd.icon!=null?cmd.icon:"";
            if (!icon.isBlank()) {
                renderIconSmall(ctx,icon,r.x()+PAD,r.y()+(r.h()-10)/2,10);
                textOffX+=12;
            }
            String lbl=(cmd.label!=null&&!cmd.label.isBlank())?cmd.label:cmd.getFirstAction();
            if (lbl==null||lbl.isBlank()) lbl="?";
            String kb=cmd.getKeybind();
            int maxW=r.w()-textOffX-PAD-(kb.isBlank()?0:70);
            while(lbl.length()>1&&textRenderer.getWidth(lbl)>maxW) lbl=lbl.substring(0,lbl.length()-1);
            ctx.drawTextWithShadow(textRenderer,Text.literal(lbl),r.x()+textOffX,r.y()+(r.h()-8)/2,C_TEXT);
            if (!kb.isBlank()) {
                String kbText=KeybindManager.formatKeybind(kb);
                ctx.drawTextWithShadow(textRenderer,Text.literal(kbText),r.x()+r.w()-PAD-textRenderer.getWidth(kbText),r.y()+(r.h()-8)/2,C_TEXT_DIM);
            }
        }
        String cntStr=QuickCmdsConfig.getCommands().size()+"/"+QuickCmdsConfig.MAX_SLOTS;
        ctx.drawTextWithShadow(textRenderer,Text.literal(cntStr),listX+listW-textRenderer.getWidth(cntStr),listY+listH-10,C_TEXT_HINT);
        int total=QuickCmdsConfig.getCommands().size();
        if (total>rRows.size()&&!rRows.isEmpty()) {
            int sy=rRows.get(0).y(), ey=rRows.get(rRows.size()-1).y()+ROW_H, totalH2=ey-sy;
            float pct=(float)scrollOffset/Math.max(1,total-rRows.size());
            int barH=Math.max(12,totalH2/Math.max(1,total)), barY=sy+(int)((totalH2-barH)*pct);
            int bx=listX+listW-SCROLL_BAR_W;
            ctx.fill(bx,sy,bx+SCROLL_BAR_W,ey,0x33FFFFFF);
            ctx.fill(bx,barY,bx+SCROLL_BAR_W,barY+barH,0xAAFFFFFF);
        }
    }

    private void renderRowsSettings(DrawContext ctx, int mx, int my) {
        hoverRow=-1;
        
        if(!rRows.isEmpty()){
            int cardTop=rRows.get(0).y()-LABEL_GAP;
            var lastGeneral=rRows.get(Math.min(2,rRows.size()-1));
            int cardBottom=lastGeneral.y()+lastGeneral.h();
            renderCard(ctx,cardTop,cardBottom);
        }
        ctx.drawTextWithShadow(textRenderer,tSectionGeneral,listX,rRows.get(0).y()-10,C_TEXT_DIM);
        Text[] sLabels={tLabelScale,tLabelKeyPanel,tLabelKeySettings};
        for (int i=0;i<rRows.size()&&i<sLabels.length;i++) {
            var r=rRows.get(i); boolean hov=r.contains(mx,my); if(hov)hoverRow=i;
            ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_ROW_HOV:C_BG_SECTION);
            ctx.drawTextWithShadow(textRenderer,sLabels[i],r.x()+PAD,r.y()+(r.h()-8)/2,C_TEXT);
            switch(i) {
                case 0->renderScaleRow(ctx,mx,my,r,hov);
                case 1->{boolean cap=capturingPanel;String t=cap?tPressKey.getString():keyPanelStr();ctx.drawTextWithShadow(textRenderer,Text.literal(t),r.x()+r.w()-PAD-textRenderer.getWidth(t),r.y()+(r.h()-8)/2,cap?0xFFDDDD44:C_TEXT_DIM);}
                case 2->{boolean cap=capturingSettings;String t=cap?tPressKey.getString():keySettingsStr();ctx.drawTextWithShadow(textRenderer,Text.literal(t),r.x()+r.w()-PAD-textRenderer.getWidth(t),r.y()+(r.h()-8)/2,cap?0xFFDDDD44:C_TEXT_DIM);}
            }
        }
        if (rSliderOuter!=null) {
            
            int cardTop=rSliderOuter.y()-LABEL_GAP;
            int cardBottom=rSliderDead.y()+rSliderDead.h();
            renderCard(ctx,cardTop,cardBottom);
            ctx.drawTextWithShadow(textRenderer,tSectionSizes,listX,rSliderOuter.y()-10,C_TEXT_DIM);
            renderSizeSlider(ctx,mx,my,rSliderOuter,QuickCmdsConfig.getOuterRadius(),QuickCmdsConfig.OUTER_R_MIN,QuickCmdsConfig.OUTER_R_MAX,tLabelOuterR.getString(),outerStr(),sliderOuterDragging);
            renderSizeSlider(ctx,mx,my,rSliderInner,QuickCmdsConfig.getInnerRadius(),QuickCmdsConfig.INNER_R_MIN,QuickCmdsConfig.INNER_R_MAX,tLabelInnerR.getString(),innerStr(),sliderInnerDragging);
            renderSizeSlider(ctx,mx,my,rSliderDead, QuickCmdsConfig.getDeadRadius(), QuickCmdsConfig.DEAD_R_MIN, QuickCmdsConfig.DEAD_R_MAX, tLabelDeadR.getString(), deadStr(), sliderDeadDragging);
        }
    }

    private void renderSizeSlider(DrawContext ctx,int mx,int my,Rect r,int value,int min,int max,String label,String valStr,boolean dragging){
        boolean hov=r.contains(mx,my)||dragging;
        ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_ROW_HOV:C_BG_SECTION);
        ctx.drawTextWithShadow(textRenderer,Text.literal(label),r.x()+PAD,r.y()+(r.h()-8)/2,C_TEXT);
        int valW=textRenderer.getWidth(valStr);
        if(hov){
            int sliderX=r.x()+r.w()/2, sliderW=r.x()+r.w()-PAD-valW-6-sliderX;
            int sliderY=r.y()+ROW_H/2-1; float pct=(float)(value-min)/(max-min); int fw=(int)(sliderW*pct);
            ctx.fill(sliderX,sliderY,sliderX+sliderW,sliderY+3,0xFF1A1A2A);
            ctx.fill(sliderX,sliderY,sliderX+fw,sliderY+3,C_TAB_UL);
            int kx=sliderX+fw; ctx.fill(kx-2,sliderY-3,kx+3,sliderY+6,0xFFDDDDDD);
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT);
        } else {
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT_DIM);
        }
    }

    private void renderScaleRow(DrawContext ctx,int mx,int my,Rect r,boolean hov){
        float scale=QuickCmdsConfig.getMenuScale(); String scaleStr=scaleStr(); int sw=textRenderer.getWidth(scaleStr);
        if(hov||sliderDragging){
            int sliderX=r.x()+r.w()/2, sliderW=r.x()+r.w()-PAD-sw-6-sliderX, sliderY=r.y()+ROW_H/2-1;
            rSlider=new Rect(sliderX,r.y(),sliderW,ROW_H);
            float pct=(scale-QuickCmdsConfig.SCALE_MIN)/(QuickCmdsConfig.SCALE_MAX-QuickCmdsConfig.SCALE_MIN);
            int filled=(int)(sliderW*pct), kx=sliderX+filled;
            ctx.fill(sliderX,sliderY,sliderX+sliderW,sliderY+3,0xFF1A1A2A);
            ctx.fill(sliderX,sliderY,sliderX+filled,sliderY+3,C_TAB_UL);
            ctx.fill(kx-2,sliderY-3,kx+3,sliderY+6,0xFFDDDDDD);
            ctx.drawTextWithShadow(textRenderer,Text.literal(scaleStr),r.x()+r.w()-PAD-sw,r.y()+(ROW_H-8)/2,C_TEXT);
        } else {
            rSlider=null;
            ctx.drawTextWithShadow(textRenderer,Text.literal(scaleStr),r.x()+r.w()-PAD-sw,r.y()+(ROW_H-8)/2,C_TEXT_DIM);
        }
    }

    private void renderTabView(DrawContext ctx, int mx, int my) {
        int y=listY+LABEL_GAP+4;
        int cardTop=y-LABEL_GAP;

        int card1Bottom=y+FIELD_H+SECTION_GAP+ROW_H*2+1;
        renderCard(ctx,cardTop,card1Bottom);

        ctx.drawTextWithShadow(textRenderer,tAppColor,listX,y-10,C_TEXT_DIM);
        ctx.fill(listX,y,listX+listW-ROW_H-PAD,y+FIELD_H,0xBB080810);
        if(rColorPreview!=null){
            int pc=0xFF000000|(QuickCmdsConfig.getSegColorR()<<16)|(QuickCmdsConfig.getSegColorG()<<8)|QuickCmdsConfig.getSegColorB();
            ctx.fill(rColorPreview.x(),rColorPreview.y(),rColorPreview.x()+rColorPreview.w(),rColorPreview.y()+rColorPreview.h(),pc);
            drawBorder(ctx,rColorPreview.x()-1,rColorPreview.y()-1,rColorPreview.w()+2,rColorPreview.h()+2,0x88FFFFFF);
        }
        y+=FIELD_H+SECTION_GAP;
        ctx.drawTextWithShadow(textRenderer,tAppAlpha,listX,y-10,C_TEXT_DIM);
        if(rSlider!=null)renderAlphaSlider(ctx,mx,my,rSlider,QuickCmdsConfig.getSegAlpha(),tAppAlphaNorm.getString(),alphaNormStr(),sliderDragging);
        if(rSliderHov!=null)renderAlphaSlider(ctx,mx,my,rSliderHov,QuickCmdsConfig.getSegAlphaHov(),tAppAlphaHov.getString(),alphaHovStr(),sliderHovDragging);
        y+=ROW_H*2+1+SECTION_GAP+LABEL_GAP;

        int card2Top=y-LABEL_GAP;
        int card2Bottom=y+FIELD_H;
        renderCard(ctx,card2Top,card2Bottom);

        ctx.drawTextWithShadow(textRenderer,tAppTextColor,listX,y-10,C_TEXT_DIM);
        ctx.fill(listX,y,listX+listW-ROW_H-PAD,y+FIELD_H,0xBB080810);
        if(rTxtHovColorPreview!=null){
            int pc=0xFF000000|(QuickCmdsConfig.getTextHovR()<<16)|(QuickCmdsConfig.getTextHovG()<<8)|QuickCmdsConfig.getTextHovB();
            ctx.fill(rTxtHovColorPreview.x(),rTxtHovColorPreview.y(),rTxtHovColorPreview.x()+rTxtHovColorPreview.w(),rTxtHovColorPreview.y()+rTxtHovColorPreview.h(),pc);
            drawBorder(ctx,rTxtHovColorPreview.x()-1,rTxtHovColorPreview.y()-1,rTxtHovColorPreview.w()+2,rTxtHovColorPreview.h()+2,0x88FFFFFF);
        }
        y+=FIELD_H+SECTION_GAP+LABEL_GAP;

        if(rIconScaleSlider!=null){
            int card3Top=rIconScaleSlider.y()-LABEL_GAP;
            int card3Bottom=rIconScaleSlider.y()+rIconScaleSlider.h();
            renderCard(ctx,card3Top,card3Bottom);
            ctx.drawTextWithShadow(textRenderer,tAppIconScale,listX,rIconScaleSlider.y()-10,C_TEXT_DIM);
            renderSizeSliderFloat(ctx,mx,my,rIconScaleSlider,QuickCmdsConfig.getIconScale(),
                    QuickCmdsConfig.ICON_SCALE_MIN,QuickCmdsConfig.ICON_SCALE_MAX,iconScaleStr(),iconScaleDragging);
        }
    }

    private void renderSizeSliderFloat(DrawContext ctx,int mx,int my,Rect r,float value,float min,float max,String valStr,boolean dragging){
        boolean hov=r.contains(mx,my)||dragging;
        ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_ROW_HOV:C_BG_SECTION);
        int valW=textRenderer.getWidth(valStr);
        if(hov){
            int sliderX=r.x()+r.w()/2, sliderW=r.x()+r.w()-PAD-valW-6-sliderX;
            int sliderY=r.y()+ROW_H/2-1; float pct=(value-min)/(max-min); int fw=(int)(sliderW*pct);
            ctx.fill(sliderX,sliderY,sliderX+sliderW,sliderY+3,0xFF1A1A2A);
            ctx.fill(sliderX,sliderY,sliderX+fw,sliderY+3,C_TAB_UL);
            int kx=sliderX+fw; ctx.fill(kx-2,sliderY-3,kx+3,sliderY+6,0xFFDDDDDD);
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT);
        } else {
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT_DIM);
        }
    }

    private void renderAlphaSlider(DrawContext ctx,int mx,int my,Rect r,int alphaVal,String label,String valStr,boolean dragging){
        boolean hov=r.contains(mx,my)||dragging;
        ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_ROW_HOV:C_BG_SECTION);
        ctx.drawTextWithShadow(textRenderer,Text.literal(label),r.x()+PAD,r.y()+(r.h()-8)/2,C_TEXT);
        int valW=textRenderer.getWidth(valStr);
        if(hov){
            int sliderX=r.x()+r.w()/2, sliderW=r.x()+r.w()-PAD-valW-6-sliderX, sliderY=r.y()+ROW_H/2-1;
            float filled=(float)(alphaVal-QuickCmdsConfig.ALPHA_MIN)/(QuickCmdsConfig.ALPHA_MAX-QuickCmdsConfig.ALPHA_MIN);
            int fw=(int)(sliderW*filled), kx=sliderX+fw;
            ctx.fill(sliderX,sliderY,sliderX+sliderW,sliderY+3,0xFF1A1A2A);
            ctx.fill(sliderX,sliderY,sliderX+fw,sliderY+3,C_TAB_UL);
            ctx.fill(kx-2,sliderY-3,kx+3,sliderY+6,0xFFDDDDDD);
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT);
        } else {
            ctx.drawTextWithShadow(textRenderer,Text.literal(valStr),r.x()+r.w()-PAD-valW,r.y()+(ROW_H-8)/2,C_TEXT_DIM);
        }
    }

    private void renderBottomBar(DrawContext ctx, int mx, int my) {
        if (isCommandEdit) {
            renderBtn(ctx,rBtnApply,mx,my,tBtnSave.getString());
            renderBtn(ctx,rBtnDel,mx,my,slotIndex>=0?tBtnDelete.getString():tBtnCancel.getString());
        } else {
            if(rBtnApplySettings!=null) renderBtnColored(ctx,rBtnApplySettings,mx,my,tBtnApply.getString(),hasUnsavedChanges);
            renderBtn(ctx,rBtnDone,mx,my,tBtnDone.getString());
            if(rBtnAdd!=null&&QuickCmdsConfig.canAdd()) renderBtn(ctx,rBtnAdd,mx,my,tBtnAdd.getString());
        }
    }

    private void renderCommandEdit(DrawContext ctx, int mx, int my) {
        ctx.drawCenteredTextWithShadow(textRenderer,slotIndex==-1?tSlotNew:tSlotEdit,width/2,(TAB_H-8)/2,C_TEXT);
        ctx.fill(listX,listY,listX+listW,bottomY,C_BG_SECTION);

        if (rDropTrig!=null) {
            boolean ddHov=rDropTrig.contains(mx,my);
            ctx.fill(rDropTrig.x(),rDropTrig.y(),rDropTrig.x()+rDropTrig.w(),rDropTrig.y()+rDropTrig.h(),ddHov?C_BG_ROW_HOV:C_BG_SECTION);
            ctx.drawTextWithShadow(textRenderer,cachedTypeLabelText[actionType],rDropTrig.x()+PAD,rDropTrig.y()+(rDropTrig.h()-8)/2,C_TEXT);
            String arrow=dropdownOpen?"^":"v";
            ctx.drawTextWithShadow(textRenderer,Text.literal(arrow),rDropTrig.x()+rDropTrig.w()-PAD-textRenderer.getWidth(arrow),rDropTrig.y()+(rDropTrig.h()-8)/2,C_TEXT_DIM);
        }

        int y=listY+PAD*2+ROW_H+LABEL_GAP;

        if (actionType==2) {
            
            ctx.drawTextWithShadow(textRenderer,tFieldLabelShort,listX,y-10,labelError?C_TEXT_ERR:C_TEXT_DIM);
            renderFieldBg(ctx,listX,y,listW,FIELD_H,labelError);
            y+=FIELD_H+PAD*2;
            if(rBtnAssign!=null){
                boolean cap=capturingSlotKey, assnHov=rBtnAssign.contains(mx,my);
                ctx.fill(rBtnAssign.x(),rBtnAssign.y(),rBtnAssign.x()+rBtnAssign.w(),rBtnAssign.y()+rBtnAssign.h(),cap?C_BG_BTN_HOV:(assnHov?C_BG_ROW_HOV:C_BG_SECTION));
                ctx.drawCenteredTextWithShadow(textRenderer,tHotkeyAssign,rBtnAssign.x()+rBtnAssign.w()/2,rBtnAssign.y()+(rBtnAssign.h()-8)/2,cap?0xFFDDDD44:C_TEXT);
                y=rBtnAssign.y()+rBtnAssign.h()+PAD;
            }
            String displayCombo=capturingSlotKey?(capturedCombo.isEmpty()?"...":String.join("+",capturedCombo)+"+")+"...":(currentKeybind.isBlank()?tHotkeyNone.getString():KeybindManager.formatKeybind(currentKeybind));
            ctx.fill(listX,y,listX+listW,y+ROW_H,C_BG_SECTION);
            ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(displayCombo),width/2,y+(ROW_H-8)/2,capturingSlotKey?0xFFDDDD44:C_TEXT_DIM);
            if(capturingSlotKey) ctx.drawCenteredTextWithShadow(textRenderer,tHotkeyEscCancel,width/2,y+ROW_H+4,C_TEXT_HINT);
            y+=ROW_H+PAD*2;

            if(rBtnIcon!=null){
                boolean bHov=rBtnIcon.contains(mx,my);
                ctx.fill(rBtnIcon.x(),rBtnIcon.y(),rBtnIcon.x()+rBtnIcon.w(),rBtnIcon.y()+rBtnIcon.h(),bHov?C_BG_BTN_HOV:C_BG_BTN);
                if(!editIcon.isBlank()){
                    renderIconSmall(ctx,editIcon,rBtnIcon.x()+PAD,rBtnIcon.y()+(ROW_H-10)/2,10);
                    ctx.drawTextWithShadow(textRenderer,tBtnIcon,rBtnIcon.x()+PAD+12,rBtnIcon.y()+(ROW_H-8)/2,C_TEXT);
                } else {
                    ctx.drawCenteredTextWithShadow(textRenderer,tBtnIcon,rBtnIcon.x()+rBtnIcon.w()/2,rBtnIcon.y()+(ROW_H-8)/2,C_TEXT);
                }
            }
            
            if(rIconPreview!=null){
                ctx.fill(rIconPreview.x(),rIconPreview.y(),rIconPreview.x()+rIconPreview.w(),rIconPreview.y()+rIconPreview.h(),0x44FFFFFF);
                drawBorder(ctx,rIconPreview.x()-1,rIconPreview.y()-1,rIconPreview.w()+2,rIconPreview.h()+2,0x66FFFFFF);
                if(!editIcon.isBlank()) renderIconFull(ctx,editIcon,rIconPreview.x(),rIconPreview.y(),rIconPreview.w());
                else ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("?"),rIconPreview.x()+rIconPreview.w()/2,rIconPreview.y()+(rIconPreview.h()-8)/2,C_TEXT_HINT);
            }

            if(labelError) ctx.drawCenteredTextWithShadow(textRenderer,tErrorFillLabel,width/2,bottomY-14,C_TEXT_ERR);
        } else {
            
            ctx.drawTextWithShadow(textRenderer,tFieldLabelShort,listX,y-10,labelError?C_TEXT_ERR:C_TEXT_DIM);
            renderFieldBg(ctx,listX,y,listW,FIELD_H,labelError);
            y+=FIELD_H+LABEL_GAP;

            Text taLabel = actionType==1 ? tFieldText : tFieldActions;
            ctx.drawTextWithShadow(textRenderer,taLabel,listX,y-10,actionError?C_TEXT_ERR:C_TEXT_DIM);
            int taH=FIELD_H*textareaRows()+FIELD_H;
            renderFieldBg(ctx,listX,y,listW,Math.max(FIELD_H,taH),actionError);
            y+=Math.max(FIELD_H,taH)+PAD*2;

            if(rDelaySlider!=null){
                boolean dHov=rDelaySlider.contains(mx,my)||delayDragging;
                ctx.fill(rDelaySlider.x(),rDelaySlider.y(),rDelaySlider.x()+rDelaySlider.w(),rDelaySlider.y()+rDelaySlider.h(),dHov?C_BG_ROW_HOV:C_BG_SECTION);
                ctx.drawTextWithShadow(textRenderer,tFieldDelay,rDelaySlider.x()+PAD,rDelaySlider.y()+(ROW_H-8)/2,C_TEXT);
                String dStr=editDelay==0?"OFF":String.format("%.0fs",editDelay);
                if(cachedDelayStr.isEmpty()||!cachedDelayStr.equals(dStr)) cachedDelayStr=dStr;
                int dW=textRenderer.getWidth(cachedDelayStr);
                if(dHov){
                    int sliderX=rDelaySlider.x()+rDelaySlider.w()/2;
                    int sliderW=rDelaySlider.x()+rDelaySlider.w()-PAD-dW-6-sliderX;
                    int sliderY2=rDelaySlider.y()+ROW_H/2-1;
                    float pct=editDelay/QuickCmdsConfig.DELAY_MAX;
                    int fw=(int)(sliderW*pct), kx=sliderX+fw;
                    ctx.fill(sliderX,sliderY2,sliderX+sliderW,sliderY2+3,0xFF1A1A2A);
                    ctx.fill(sliderX,sliderY2,sliderX+fw,sliderY2+3,C_TAB_UL);
                    ctx.fill(kx-2,sliderY2-3,kx+3,sliderY2+6,0xFFDDDDDD);
                    ctx.drawTextWithShadow(textRenderer,Text.literal(cachedDelayStr),rDelaySlider.x()+rDelaySlider.w()-PAD-dW,rDelaySlider.y()+(ROW_H-8)/2,C_TEXT);
                } else {
                    ctx.drawTextWithShadow(textRenderer,Text.literal(cachedDelayStr),rDelaySlider.x()+rDelaySlider.w()-PAD-dW,rDelaySlider.y()+(ROW_H-8)/2,C_TEXT_DIM);
                }
            }

            if(rBtnIcon!=null){
                boolean bHov=rBtnIcon.contains(mx,my);
                ctx.fill(rBtnIcon.x(),rBtnIcon.y(),rBtnIcon.x()+rBtnIcon.w(),rBtnIcon.y()+rBtnIcon.h(),bHov?C_BG_BTN_HOV:C_BG_BTN);
                
                if(!editIcon.isBlank()){
                    renderIconSmall(ctx,editIcon,rBtnIcon.x()+PAD,rBtnIcon.y()+(ROW_H-10)/2,10);
                    ctx.drawTextWithShadow(textRenderer,tBtnIcon,rBtnIcon.x()+PAD+12,rBtnIcon.y()+(ROW_H-8)/2,C_TEXT);
                } else {
                    ctx.drawCenteredTextWithShadow(textRenderer,tBtnIcon,rBtnIcon.x()+rBtnIcon.w()/2,rBtnIcon.y()+(ROW_H-8)/2,C_TEXT);
                }
            }
            
            if(rIconPreview!=null){
                ctx.fill(rIconPreview.x(),rIconPreview.y(),rIconPreview.x()+rIconPreview.w(),rIconPreview.y()+rIconPreview.h(),0x44FFFFFF);
                drawBorder(ctx,rIconPreview.x()-1,rIconPreview.y()-1,rIconPreview.w()+2,rIconPreview.h()+2,0x66FFFFFF);
                if(!editIcon.isBlank()) renderIconFull(ctx,editIcon,rIconPreview.x(),rIconPreview.y(),rIconPreview.w());
                else ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("?"),rIconPreview.x()+rIconPreview.w()/2,rIconPreview.y()+(rIconPreview.h()-8)/2,C_TEXT_HINT);
            }

            if(labelError||actionError){
                String msg=tErrorFill.getString()+(labelError?tFieldLabelShort.getString()+" ":"")+(actionError?taLabel.getString():"");
                ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(msg),width/2,bottomY-14,C_TEXT_ERR);
            }
        }
    }

    private void renderIconSmall(DrawContext ctx, String icon, int x, int y, int size) {
        if (icon.startsWith("item:")) {
            try {
                Item item = Registries.ITEM.get(Identifier.of(icon.substring(5)));
                
                int offset = (size - 16) / 2;
                ctx.drawItem(new ItemStack(item), x + offset, y + offset);
            } catch (Exception ignored) {}
        } else if (icon.startsWith("sprite:")) {
            try {
                ctx.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        Identifier.of(icon.substring(7)), x, y, size, size);
            } catch (Exception ignored) {}
        }
    }

    private void renderIconFull(DrawContext ctx, String icon, int x, int y, int size) {
        if (icon.startsWith("item:")) {
            try {
                Item item = Registries.ITEM.get(Identifier.of(icon.substring(5)));
                int offset = (size - 16) / 2;
                ctx.drawItem(new ItemStack(item), x + offset, y + offset);
            } catch (Exception ignored) {}
        } else if (icon.startsWith("sprite:")) {
            try {
                ctx.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                        Identifier.of(icon.substring(7)), x, y, size, size);
            } catch (Exception ignored) {}
        }
    }

    private void renderDropdown(DrawContext ctx, int mx, int my) {
        rDropItems.clear();
        int dy=rDropTrig.y()+rDropTrig.h()+2, totalH=typeLabels().length*(ROW_H+1);
        ctx.fill(listX,dy,listX+rDropTrig.w(),dy+totalH,C_BG_DROPDOWN);
        for (int i=0;i<typeLabels().length;i++) {
            int iy=dy+i*(ROW_H+1); var r=new Rect(listX,iy,rDropTrig.w(),ROW_H); rDropItems.add(r);
            boolean hov=r.contains(mx,my), sel=(actionType==i);
            ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),sel?C_BG_BTN_HOV:(hov?C_BG_ROW_HOV:0));
            ctx.drawTextWithShadow(textRenderer,cachedTypeLabelText[i],r.x()+PAD,r.y()+(r.h()-8)/2,sel||hov?C_TEXT:C_TEXT_DIM);
        }
    }

    private List<Text> buildCmdTooltip(QuickCommand cmd) {
        List<Text> lines=new ArrayList<>();
        String head=(cmd.label!=null&&!cmd.label.isBlank())?cmd.label:cachedTypeLabels[0];
        lines.add(Text.literal(head));
        int ti=switch(cmd.type==null?"":cmd.type){case"chat"->1;case"hotkey"->2;default->0;};
        lines.add(Text.literal("\u00A77"+tTooltipType.getString()+typeLabels()[ti]));
        List<String> acts=cmd.getActions();
        if(!acts.isEmpty()&&!cmd.isHotkey()) {
            for (int i=0;i<Math.min(3,acts.size());i++) lines.add(Text.literal("\u00A77"+acts.get(i)));
            if(acts.size()>3) lines.add(Text.literal("\u00A78… +"+(acts.size()-3)));
        }
        if(cmd.delay>0) lines.add(Text.literal("\u00A78"+tTooltipDelay.getString()+": "+String.format("%.0fs",cmd.delay)));
        String kb=cmd.getKeybind(); if(!kb.isBlank()) lines.add(Text.literal("\u00A7e"+KeybindManager.formatKeybind(kb)));
        lines.add(Text.literal("")); lines.add(tTooltipHint);
        return lines;
    }

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        int x=(int)mx, y=(int)my;

        if (!isCommandEdit&&activeTab==0&&btn==0) {
            int bx=listX+listW-SCROLL_BAR_W;
            if(x>=bx&&x<bx+SCROLL_BAR_W&&!rRows.isEmpty()){scrollBarDragging=true;scrollBarAnchorY=my;scrollBarAnchorOffset=scrollOffset;return true;}
            touchScrollAnchorY=my; touchScrollAnchorOffset=scrollOffset; touchScrolling=false;
        }

        if (isCommandEdit) {
            if (dropdownOpen) {
                for(int i=0;i<rDropItems.size();i++) if(rDropItems.get(i).contains(x,y)){if(actionType!=i){playClick();actionType=i;clearChildren();init();}dropdownOpen=false;return true;}
                dropdownOpen=false; return true;
            }
            if(rDropTrig!=null&&rDropTrig.contains(x,y)){playClick();dropdownOpen=!dropdownOpen;return true;}
            if(rBtnAssign!=null&&rBtnAssign.contains(x,y)){playClick();capturingSlotKey=true;capturedCombo.clear();return true;}
            
            if(rDelaySlider!=null&&rDelaySlider.contains(x,y)){delayDragging=true;applyDelaySliderX(x);return true;}
            
            if(rBtnIcon!=null&&rBtnIcon.contains(x,y)){
                playClick();
                
                savedLabel   = labelField   != null ? labelField.getText()   : savedLabel;
                savedActions = actionsField != null ? actionsField.getText() : savedActions;
                client.setScreen(new IconPickerScreen(this, editIcon, icon -> { this.editIcon = icon; }));
                return true;
            }
            if(rBtnApply!=null&&rBtnApply.contains(x,y)){playClick();save();return true;}
            if(rBtnDel!=null&&rBtnDel.contains(x,y)){playClick();if(slotIndex>=0){QuickCmdsConfig.remove(slotIndex);QuickCmdsConfig.save();}close();return true;}
        } else {
            for(int i=0;i<rTabs.size();i++) if(rTabs.get(i).contains(x,y)&&activeTab!=i){playClick();activeTab=i;scrollOffset=0;hoverRow=-1;sliderDragging=false;sliderHovDragging=false;hasUnsavedChanges=false;clearChildren();init();return true;}
            if(activeTab==0){
                var cmds=QuickCmdsConfig.getCommands();
                for(int vi=0;vi<rRows.size();vi++){int idx=vi+scrollOffset;if(idx>=cmds.size())break;if(rRows.get(vi).contains(x,y)){if(btn==0){playClick();client.setScreen(new EditCommandScreen(this,idx));return true;}if(btn==1){playClick();QuickCmdsConfig.remove(idx);QuickCmdsConfig.save();scrollOffset=Math.max(0,scrollOffset-1);clearChildren();init();return true;}}}
                if(rBtnAdd!=null&&rBtnAdd.contains(x,y)){playClick();client.setScreen(new EditCommandScreen(this,-1));return true;}
            } else if(activeTab==1){
                if(rBtnKeyPanel!=null&&rBtnKeyPanel.contains(x,y)){playClick();capturingPanel=true;capturingSettings=false;return true;}
                if(rBtnKeySettings!=null&&rBtnKeySettings.contains(x,y)){playClick();capturingSettings=true;capturingPanel=false;return true;}
                if(rSlider!=null&&rSlider.contains(x,y)){sliderDragging=true;applyScaleSliderX(x);hasUnsavedChanges=true;return true;}
                if(rSliderOuter!=null&&rSliderOuter.contains(x,y)){sliderOuterDragging=true;applySizeSliderX(x,rSliderOuter,QuickCmdsConfig.OUTER_R_MIN,QuickCmdsConfig.OUTER_R_MAX,0);hasUnsavedChanges=true;return true;}
                if(rSliderInner!=null&&rSliderInner.contains(x,y)){sliderInnerDragging=true;applySizeSliderX(x,rSliderInner,QuickCmdsConfig.INNER_R_MIN,QuickCmdsConfig.INNER_R_MAX,1);hasUnsavedChanges=true;return true;}
                if(rSliderDead!=null&&rSliderDead.contains(x,y)){sliderDeadDragging=true;applySizeSliderX(x,rSliderDead,QuickCmdsConfig.DEAD_R_MIN,QuickCmdsConfig.DEAD_R_MAX,2);hasUnsavedChanges=true;return true;}
            } else if(activeTab==2){
                if(rSlider!=null&&rSlider.contains(x,y)){sliderDragging=true;applyAlphaSliderX(x,false);hasUnsavedChanges=true;return true;}
                if(rSliderHov!=null&&rSliderHov.contains(x,y)){sliderHovDragging=true;applyAlphaSliderX(x,true);hasUnsavedChanges=true;return true;}
                if(rIconScaleSlider!=null&&rIconScaleSlider.contains(x,y)){iconScaleDragging=true;applyIconScaleSliderX(x);hasUnsavedChanges=true;return true;}
            }
            if(rBtnApplySettings!=null&&rBtnApplySettings.contains(x,y)&&hasUnsavedChanges){playClick();applyHexField();QuickCmdsConfig.save();hasUnsavedChanges=false;return true;}
            if(rBtnDone!=null&&rBtnDone.contains(x,y)){playClick();close();return true;}
        }
        return super.mouseClicked(mx,my,btn);
    }

    @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){
        if(!isCommandEdit&&activeTab==0){int total=QuickCmdsConfig.getCommands().size();scrollOffset=Math.max(0,Math.min(scrollOffset+(dy>0?-1:1),total-1));clearChildren();init();return true;}
        return super.mouseScrolled(mx,my,dx,dy);
    }

    @Override public boolean mouseDragged(double mx,double my,int btn,double dx,double dy){
        if(scrollBarDragging&&!rRows.isEmpty()){
            int total=QuickCmdsConfig.getCommands().size(),vis=rRows.size();
            int sy=rRows.get(0).y(),ey=rRows.get(vis-1).y()+ROW_H,totalH2=ey-sy;
            int barH=Math.max(12,totalH2/Math.max(1,total)),trackH=totalH2-barH;
            if(trackH>0){int steps=(int)((my-scrollBarAnchorY)/trackH*Math.max(1,total-vis));int no=Math.max(0,Math.min(scrollBarAnchorOffset+steps,Math.max(0,total-vis)));if(no!=scrollOffset){scrollOffset=no;clearChildren();init();}}
            return true;
        }
        if(!isCommandEdit&&activeTab==0&&btn==0){
            double totalDelta=touchScrollAnchorY-my;
            if(!touchScrolling&&Math.abs(totalDelta)>TOUCH_SCROLL_THRESHOLD) touchScrolling=true;
            if(touchScrolling){int steps=(int)((touchScrollAnchorY-my)/ROW_H);int total=QuickCmdsConfig.getCommands().size(),vis=Math.max(1,rRows.size());int no=Math.max(0,Math.min(touchScrollAnchorOffset+steps,Math.max(0,total-vis)));if(no!=scrollOffset){scrollOffset=no;clearChildren();init();}return true;}
        }
        if(delayDragging){applyDelaySliderX((int)mx);return true;}
        if(sliderDragging){if(activeTab==1)applyScaleSliderX((int)mx);else applyAlphaSliderX((int)mx,false);hasUnsavedChanges=true;return true;}
        if(sliderHovDragging){applyAlphaSliderX((int)mx,true);hasUnsavedChanges=true;return true;}
        if(iconScaleDragging){applyIconScaleSliderX((int)mx);hasUnsavedChanges=true;return true;}
        if(sliderOuterDragging){applySizeSliderX((int)mx,rSliderOuter,QuickCmdsConfig.OUTER_R_MIN,QuickCmdsConfig.OUTER_R_MAX,0);hasUnsavedChanges=true;return true;}
        if(sliderInnerDragging){applySizeSliderX((int)mx,rSliderInner,QuickCmdsConfig.INNER_R_MIN,QuickCmdsConfig.INNER_R_MAX,1);hasUnsavedChanges=true;return true;}
        if(sliderDeadDragging){applySizeSliderX((int)mx,rSliderDead,QuickCmdsConfig.DEAD_R_MIN,QuickCmdsConfig.DEAD_R_MAX,2);hasUnsavedChanges=true;return true;}
        return super.mouseDragged(mx,my,btn,dx,dy);
    }

    @Override public boolean mouseReleased(double mx,double my,int btn){
        boolean wasScrolling=touchScrolling; touchScrolling=false;
        if(scrollBarDragging){scrollBarDragging=false;return true;}
        delayDragging=false;
        if(sliderDragging||sliderHovDragging||sliderOuterDragging||sliderInnerDragging||sliderDeadDragging||iconScaleDragging){sliderDragging=sliderHovDragging=sliderOuterDragging=sliderInnerDragging=sliderDeadDragging=iconScaleDragging=false;hasUnsavedChanges=true;return true;}
        if(wasScrolling) return true;
        return super.mouseReleased(mx,my,btn);
    }

    private void applyDelaySliderX(int mx) {
        if(rDelaySlider==null) return;
        int sliderX=rDelaySlider.x()+rDelaySlider.w()/2;
        String dStr="00s"; int dW=textRenderer.getWidth(dStr);
        int sliderW=rDelaySlider.x()+rDelaySlider.w()-PAD-dW-6-sliderX;
        if(sliderW<=0) sliderW=rDelaySlider.w()/3;
        float pct=Math.max(0,Math.min(1,(float)(mx-sliderX)/sliderW));
        
        editDelay=Math.round(pct*QuickCmdsConfig.DELAY_MAX);
        cachedDelayStr="";
    }

    private void applyScaleSliderX(int mx){
        if(rSlider==null)return;
        float pct=Math.max(0,Math.min(1,(float)(mx-rSlider.x())/rSlider.w()));
        float scale=QuickCmdsConfig.SCALE_MIN+pct*(QuickCmdsConfig.SCALE_MAX-QuickCmdsConfig.SCALE_MIN);
        QuickCmdsConfig.setMenuScale(Math.round(scale*10)/10f);
    }
    private void applySizeSliderX(int mx,Rect r,int min,int max,int which){
        if(r==null)return;
        int sliderX=r.x()+r.w()/2, valW=textRenderer.getWidth(max+"px"), sliderW=r.x()+r.w()-PAD-valW-6-sliderX;
        if(sliderW<=0)sliderW=r.w()/3;
        float pct=Math.max(0,Math.min(1,(float)(mx-sliderX)/sliderW));
        int val=min+Math.round(pct*(max-min));
        switch(which){case 0->QuickCmdsConfig.setOuterRadius(val);case 1->QuickCmdsConfig.setInnerRadius(val);case 2->QuickCmdsConfig.setDeadRadius(val);}
    }
    private void applyAlphaSliderX(int mx,boolean isHov){
        Rect r=isHov?rSliderHov:rSlider; if(r==null)return;
        int sliderX=r.x()+r.w()/2, sliderW=r.w()/2-30; if(sliderW<=0)sliderW=r.w()/3;
        float pct=Math.max(0,Math.min(1,(float)(mx-sliderX)/sliderW));
        int alpha=QuickCmdsConfig.ALPHA_MIN+(int)(pct*(QuickCmdsConfig.ALPHA_MAX-QuickCmdsConfig.ALPHA_MIN));
        if(isHov)QuickCmdsConfig.setSegAlphaHov(alpha); else QuickCmdsConfig.setSegAlpha(alpha);
    }

    private void applyIconScaleSliderX(int mx){
        if(rIconScaleSlider==null)return;
        int sliderX=rIconScaleSlider.x()+rIconScaleSlider.w()/2;
        int valW=textRenderer.getWidth("2.0×");
        int sliderW=rIconScaleSlider.x()+rIconScaleSlider.w()-PAD-valW-6-sliderX;
        if(sliderW<=0)sliderW=rIconScaleSlider.w()/3;
        float pct=Math.max(0,Math.min(1,(float)(mx-sliderX)/sliderW));
        float val=QuickCmdsConfig.ICON_SCALE_MIN+pct*(QuickCmdsConfig.ICON_SCALE_MAX-QuickCmdsConfig.ICON_SCALE_MIN);
        QuickCmdsConfig.setIconScale(Math.round(val*10)/10f);
    }

    @Override public boolean keyPressed(int kc,int sc,int mod){
        if(capturingSlotKey){
            if(kc==GLFW.GLFW_KEY_ESCAPE){capturingSlotKey=false;capturedCombo.clear();return true;}
            String m=modName(kc); if(m!=null){if(!capturedCombo.contains(m))capturedCombo.add(m);}
            else{String k=keyName(kc);if(k!=null){capturedCombo.removeIf(p->!p.equals("CTRL")&&!p.equals("SHIFT")&&!p.equals("ALT"));capturedCombo.add(k);}}
            return true;
        }
        if(capturingPanel||capturingSettings){
            if(kc==GLFW.GLFW_KEY_ESCAPE){capturingPanel=capturingSettings=false;return true;}
            var kb=InputUtil.fromKeyCode(kc,sc);
            if(capturingPanel)KeybindManager.OPEN_PANEL.setBoundKey(kb); else KeybindManager.OPEN_SETTINGS.setBoundKey(kb);
            net.minecraft.client.option.KeyBinding.updateKeysByCode();
            capturingPanel=capturingSettings=false;hasUnsavedChanges=true;clearChildren();init();return true;
        }
        if(activeTab==2&&kc==GLFW.GLFW_KEY_ENTER){applyHexField();hasUnsavedChanges=true;return true;}
        if(kc==GLFW.GLFW_KEY_ESCAPE){close();return true;}
        return super.keyPressed(kc,sc,mod);
    }

    @Override public boolean keyReleased(int kc,int sc,int mod){
        if(capturingSlotKey&&!capturedCombo.isEmpty()){
            List<String> mods2=new ArrayList<>(),keys=new ArrayList<>();
            for(String p:capturedCombo){if(p.equals("CTRL")||p.equals("SHIFT")||p.equals("ALT"))mods2.add(p);else keys.add(p);}
            if(!keys.isEmpty()){List<String> ord=new ArrayList<>(mods2);ord.addAll(keys);currentKeybind=String.join("+",ord);capturingSlotKey=false;capturedCombo.clear();}
            else{String rel=modName(kc);if(rel!=null)capturedCombo.remove(rel);}
            return true;
        }
        return super.keyReleased(kc,sc,mod);
    }

    private String modName(int k){return switch(k){case GLFW.GLFW_KEY_LEFT_CONTROL,GLFW.GLFW_KEY_RIGHT_CONTROL->"CTRL";case GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT->"SHIFT";case GLFW.GLFW_KEY_LEFT_ALT,GLFW.GLFW_KEY_RIGHT_ALT->"ALT";default->null;};}
    private String keyName(int k){
        if(k>=GLFW.GLFW_KEY_A&&k<=GLFW.GLFW_KEY_Z)return String.valueOf((char)('A'+(k-GLFW.GLFW_KEY_A)));
        if(k>=GLFW.GLFW_KEY_0&&k<=GLFW.GLFW_KEY_9)return String.valueOf(k-GLFW.GLFW_KEY_0);
        if(k>=GLFW.GLFW_KEY_F1&&k<=GLFW.GLFW_KEY_F12)return "F"+(k-GLFW.GLFW_KEY_F1+1);
        if(k>=GLFW.GLFW_KEY_KP_0&&k<=GLFW.GLFW_KEY_KP_9)return "KP"+(k-GLFW.GLFW_KEY_KP_0);
        return switch(k){case GLFW.GLFW_KEY_SPACE->"SPACE";case GLFW.GLFW_KEY_ENTER->"ENTER";case GLFW.GLFW_KEY_TAB->"TAB";case GLFW.GLFW_KEY_BACKSPACE->"BACKSPACE";case GLFW.GLFW_KEY_DELETE->"DELETE";case GLFW.GLFW_KEY_INSERT->"INSERT";case GLFW.GLFW_KEY_HOME->"HOME";case GLFW.GLFW_KEY_END->"END";case GLFW.GLFW_KEY_PAGE_UP->"PAGEUP";case GLFW.GLFW_KEY_PAGE_DOWN->"PAGEDOWN";default->null;};
    }

    @Override public void tick(){
        super.tick();
        if(activeTab==2){
            String c1=hexField!=null?hexField.getText():"", c3=hexTxtHovField!=null?hexTxtHovField.getText():"";
            String combined=c1+"|"+c3;
            if(!combined.equals(prevHexValue)){prevHexValue=combined;hasUnsavedChanges=true;}
        }
    }

    private void applyHexField(){
        applyHex(hexField,(r,g,b)->QuickCmdsConfig.setSegColor(r,g,b));
        applyHex(hexTxtHovField,(r,g,b)->QuickCmdsConfig.setTextHovColor(r,g,b));
        hasUnsavedChanges=true;
    }
    @FunctionalInterface interface ColorSetter{void set(int r,int g,int b);}
    private void applyHex(MultilineTextFieldWidget field,ColorSetter setter){
        if(field==null)return; String hex=field.getText().trim(); if(hex.startsWith("#"))hex=hex.substring(1);
        if(hex.length()==6){try{setter.set(Integer.parseInt(hex.substring(0,2),16),Integer.parseInt(hex.substring(2,4),16),Integer.parseInt(hex.substring(4,6),16));}catch(NumberFormatException ignored){}}
    }
    private String colorToHex(int r,int g,int b){return String.format("#%02X%02X%02X",r,g,b);}

    private void save() {
        String lbl=labelField!=null?labelField.getText().trim():"";
        labelError=lbl.isEmpty();
        if (actionType==2) {
            if(labelError)return;
            QuickCommand cmd=new QuickCommand(lbl,currentKeybind,"hotkey");
            cmd.icon=editIcon.isBlank()?null:editIcon;
            applyCmd(cmd); return;
        }
        String actText=actionsField!=null?actionsField.getText().trim():"";
        actionError=actText.isEmpty();
        if(labelError||actionError) return;

        String[] lines=actText.split("\n");
        List<String> actions=new ArrayList<>();
        for (String line:lines) { String t=line.trim(); if(!t.isBlank()) actions.add(t); }
        if(actions.isEmpty()){actionError=true;return;}

        QuickCommand cmd=new QuickCommand();
        cmd.label=lbl;
        cmd.type=actionType==1?"chat":"command";
        cmd.actions=actions;
        cmd.action=actions.get(0);
        cmd.delay=editDelay;
        cmd.icon=editIcon.isBlank()?null:editIcon;
        applyCmd(cmd);
    }

    private void applyCmd(QuickCommand cmd){
        if(slotIndex==-1)QuickCmdsConfig.add(cmd); else QuickCmdsConfig.set(slotIndex,cmd);
        QuickCmdsConfig.save(); close();
    }

    @Override public void close(){dropdownOpen=false;client.setScreen(parent);}

    private void renderBtn(DrawContext ctx,Rect r,int mx,int my,String label){if(r==null)return;boolean hov=r.contains(mx,my);ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),hov?C_BG_BTN_HOV:C_BG_BTN);ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(label),r.x()+r.w()/2,r.y()+(r.h()-8)/2,C_TEXT);}
    private void renderBtnColored(DrawContext ctx,Rect r,int mx,int my,String label,boolean active){if(r==null)return;boolean hov=r.contains(mx,my)&&active;ctx.fill(r.x(),r.y(),r.x()+r.w(),r.y()+r.h(),active?(hov?C_BG_BTN_HOV:C_BG_BTN):0x55111118);ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(label),r.x()+r.w()/2,r.y()+(r.h()-8)/2,active?C_TEXT:C_TEXT_DIM);}
    private void renderFieldBg(DrawContext ctx,int x,int y,int w,int h,boolean error){ctx.fill(x,y,x+w,y+h,error?0xBB1A0808:C_BG_FIELD);}
    private void drawBorder(DrawContext ctx,int x,int y,int w,int h,int color){ctx.fill(x,y,x+w,y+1,color);ctx.fill(x,y+h-1,x+w,y+h,color);ctx.fill(x,y,x+1,y+h,color);ctx.fill(x+w-1,y,x+w,y+h,color);}
    
    private static final int CARD_SAFE_MARGIN = 4;
    private void renderCard(DrawContext ctx,int top,int bottom){
        int pad=2;
        int x=listX-pad, w=listW+pad*2;
        int y=Math.max(listY+CARD_SAFE_MARGIN,top-pad);
        int yEnd=Math.min(bottomY-CARD_SAFE_MARGIN,bottom+pad);
        if (yEnd<=y) return;
        ctx.fill(x,y,x+w,yEnd,C_CARD_BG);
    }
    private void playClick(){client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK,1.0f));}
}

