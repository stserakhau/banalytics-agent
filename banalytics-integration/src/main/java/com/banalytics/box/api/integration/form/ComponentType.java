package com.banalytics.box.api.integration.form;

public enum ComponentType {
    /**
     *
     */
    hidden,

    /**
     */
    figures_painter,

    roi_capture,

    /**
     * api-uuid - target form property which define uuid of FileSystemNavigator component
     * enableFolderSelection - when true allow to choose folder
     * enableFileSelection - when true allow to choose file
     * fileNameFilter - regular expression which define filename pattern for filter. Example: ^.*\.(wav|acc|mp3)$
     */
    folder_chooser,

    drop_down,
    multi_select,
    checkbox,

    /**
     * Data level selector
     * dataSource - source of the data for displaying graphics
     */
    level_selector,

    /**
     * uiConfig = {
     *    @UIComponent.UIConfig(name = "rest", value = "https://console.banalytics.live/api/public/camera/descriptor?producer={key}&model={key}")
     * }
     *
     * component provide search by partial values
     * UI is input where user types the value and see search result under input box, after choosing property of selected object fills the input
     * If no selection click input clear.
     */
    parametrized_rest_search,

    text_input,
    text_area,
    text_input_readonly,
    password_input,

    /**
     * min - min value, when absent then unbound
     * max - max value, when absent then unbound
     */
    int_input,
    range_input,

    task_form
}
