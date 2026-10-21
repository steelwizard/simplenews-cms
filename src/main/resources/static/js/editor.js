(function () {
    var script = document.currentScript;
    var base = (script && script.getAttribute('data-base')) || '/js/tinymce';

    if (!window.tinymce || !document.querySelector('textarea.html-editor')) {
        return;
    }

    tinymce.init({
        selector: 'textarea.html-editor',
        license_key: 'gpl',
        base_url: base,
        suffix: '.min',
        skin: 'oxide-dark',
        content_css: 'dark',
        height: 520,
        menubar: false,
        branding: false,
        promotion: false,
        browser_spellcheck: true,
        plugins: 'lists link autolink image table code',
        toolbar: 'undo redo | blocks | bold italic inlinecode | bullist numlist blockquote | link image table | code',
        block_formats: 'Paragraph=p; Heading 2=h2; Heading 3=h3; Heading 4=h4',
        // Keep the markup the sanitizer will store: no classes, no inline styles.
        convert_urls: false,
        paste_data_images: false,
        object_resizing: false,
        image_dimensions: false,
        link_title: false,
        link_target_list: false,
        table_default_attributes: {},
        table_default_styles: {},
        table_resize_bars: false,
        table_appearance_options: false,
        table_advtab: false,
        table_cell_advtab: false,
        table_row_advtab: false,
        valid_elements: 'p,div,br,span,blockquote,h1,h2,h3,h4,h5,h6,strong/b,em/i,u,s/strike,sub,sup,code,tt,big,small,ins,del,font[color|face|size],ul,ol,li,a[href],img[src|alt|width|height|border],table[summary|align],thead[align|valign],tbody[align|valign],tfoot[align|valign],caption,colgroup[align|valign],col[align|valign],tr[align|valign],th[align|valign|colspan|rowspan],td[align|valign|colspan|rowspan]',
        content_style: 'img{max-width:100%;height:auto}table,th,td{border:1px solid #6d737b}',
        setup: function (editor) {
            editor.ui.registry.addToggleButton('inlinecode', {
                text: '<>',
                tooltip: 'Inline code',
                onAction: function () {
                    editor.formatter.toggle('code');
                },
                onSetup: function (api) {
                    var binding = editor.formatter.formatChanged('code', function (state) {
                        api.setActive(state);
                    });
                    return function () {
                        if (binding) binding.unbind();
                    };
                }
            });
        }
    });
})();
