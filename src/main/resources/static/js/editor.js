(function () {
    var script = document.currentScript;
    var base = (script && script.getAttribute('data-base')) || '/js/tinymce';
    var MAX_HTML_BYTES = 1024 * 1024;
    var MAX_HTML_FILES = 10;

    if (!window.tinymce || !document.querySelector('textarea.html-editor')) {
        return;
    }

    function isHtmlFile(file) {
        var name = (file && file.name ? file.name : '').toLowerCase();
        return file && (file.type === 'text/html' || /\.html?$/.test(name));
    }

    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, function (ch) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch];
        });
    }

    function notify(editor, text, type) {
        editor.notificationManager.open({
            text: text,
            type: type || 'info',
            timeout: 5000
        });
    }

    function editorHasContent(editor) {
        var text = editor.getContent({ format: 'text' }).replace(/\u00a0/g, ' ').trim();
        if (text) return true;
        var body = editor.getBody();
        return !!(body && body.querySelector('img, table'));
    }

    function fileLabel(items) {
        if (items.length === 1) return items[0].name;
        if (items.length === 2) return items[0].name + ' and ' + items[1].name;
        return items.length + ' HTML files';
    }

    function decodeHtmlBytes(bytes) {
        if (bytes.length >= 2 && bytes[0] === 0xFF && bytes[1] === 0xFE) {
            return new TextDecoder('utf-16le').decode(bytes);
        }
        if (bytes.length >= 2 && bytes[0] === 0xFE && bytes[1] === 0xFF) {
            return new TextDecoder('utf-16be').decode(bytes);
        }
        var head = new TextDecoder('utf-8').decode(bytes.subarray(0, Math.min(bytes.length, 4096)));
        var match = /charset\s*=\s*["']?\s*([A-Za-z0-9._-]+)/i.exec(head);
        var charset = (match && match[1]) || 'utf-8';
        if (/^utf-?8$/i.test(charset)) charset = 'utf-8';
        try {
            return new TextDecoder(charset).decode(bytes);
        } catch (e) {
            return new TextDecoder('utf-8').decode(bytes);
        }
    }

    function looksBinary(bytes) {
        var limit = Math.min(bytes.length, 8000);
        for (var i = 0; i < limit; i++) {
            if (bytes[i] === 0) return true;
        }
        return false;
    }

    // Full documents contribute their body only. Scripts and styles never reach the editor.
    function extractBody(raw) {
        var doc = new DOMParser().parseFromString(raw, 'text/html');
        doc.querySelectorAll('script, style, noscript, iframe, object, embed, link, meta, title').forEach(function (node) {
            node.remove();
        });
        if (!doc.body) return '';
        var comments = [];
        var walker = doc.createTreeWalker(doc.body, NodeFilter.SHOW_COMMENT, null);
        var comment;
        while ((comment = walker.nextNode())) comments.push(comment);
        comments.forEach(function (node) { node.remove(); });
        return doc.body.innerHTML.trim();
    }

    function hasImportableContent(html) {
        var doc = new DOMParser().parseFromString('<body>' + html + '</body>', 'text/html');
        var text = (doc.body.textContent || '').replace(/\u00a0/g, ' ').trim();
        if (text) return true;
        return !!doc.body.querySelector('img, table');
    }

    function excerpt(html) {
        var doc = new DOMParser().parseFromString('<body>' + html + '</body>', 'text/html');
        var text = (doc.body.textContent || '').replace(/\s+/g, ' ').trim();
        if (!text) return 'Images or a table, with no text.';
        if (text.length > 280) return text.slice(0, 280) + '…';
        return text;
    }

    function readHtmlFile(file) {
        if (!isHtmlFile(file)) {
            return Promise.resolve({ name: file.name || 'That file', error: 'is not an HTML file' });
        }
        return file.arrayBuffer().then(function (buffer) {
            if (buffer.byteLength > MAX_HTML_BYTES) {
                return { name: file.name, error: 'is larger than 1 MB' };
            }
            var bytes = new Uint8Array(buffer);
            var utf16 = bytes.length >= 2 && (
                (bytes[0] === 0xFF && bytes[1] === 0xFE) ||
                (bytes[0] === 0xFE && bytes[1] === 0xFF)
            );
            if (!utf16 && looksBinary(bytes)) {
                return { name: file.name, error: 'does not look like HTML' };
            }
            var html = extractBody(decodeHtmlBytes(bytes));
            if (!hasImportableContent(html)) {
                return { name: file.name, error: 'has nothing to add' };
            }
            return { name: file.name, html: html };
        }, function () {
            return { name: file.name || 'That file', error: 'could not be read' };
        });
    }

    function chooseHtmlFiles() {
        return new Promise(function (resolve) {
            var input = document.createElement('input');
            input.type = 'file';
            input.accept = '.html,.htm,text/html';
            input.multiple = true;
            input.style.display = 'none';
            document.body.appendChild(input);
            input.addEventListener('change', function () {
                var files = Array.prototype.slice.call(input.files || []);
                input.remove();
                resolve(files);
            });
            input.click();
        });
    }

    function placeHtml(editor, html, mode, bookmark) {
        editor.focus();
        editor.undoManager.transact(function () {
            if (mode === 'replace' || mode === 'load') {
                editor.setContent(html);
                return;
            }
            if (mode === 'append') {
                editor.selection.select(editor.getBody(), true);
                editor.selection.collapse(false);
            } else if (bookmark) {
                editor.selection.moveToBookmark(bookmark);
            }
            editor.insertContent(html);
        });
    }

    function finishImport(editor, items, errors, mode, bookmark) {
        var html = items.map(function (item) { return item.html; }).join('\n');
        var label = fileLabel(items);
        placeHtml(editor, html, mode, bookmark);
        var where = mode === 'load' ? 'Loaded '
            : mode === 'replace' ? 'Replaced the editor contents with '
            : mode === 'append' ? 'Added at the end: '
            : mode === 'selection' ? 'Inserted in place of the selection: '
            : 'Inserted at the cursor: ';
        var text = where + label + '.';
        if (errors.length) {
            text += ' Skipped ' + errors.map(function (item) {
                return item.name;
            }).join(', ') + '.';
        }
        notify(editor, text, 'info');
    }

    function openPlacementDialog(editor, items, errors) {
        var bookmark = editor.selection.getBookmark(2, true);
        var selected = !editor.selection.isCollapsed();
        var insertLabel = selected ? 'Insert in place of selection' : 'Insert at cursor';
        var lines = [
            '<p>The editor already has content. Nothing is replaced unless you choose that.</p>',
            '<p><strong>' + items.map(function (item) {
                return escapeHtml(item.name);
            }).join('</strong><br><strong>') + '</strong></p>',
            '<p>' + escapeHtml(excerpt(items[0].html)) + '</p>'
        ];
        if (items.length > 1) {
            lines.push('<p>Files are added in the order you picked them.</p>');
        }
        if (selected) {
            lines.push('<p>Text is selected, so inserting at the cursor replaces only that selection.</p>');
        }
        if (errors.length) {
            lines.push('<p>Skipped: ' + errors.map(function (item) {
                return escapeHtml(item.name) + ' ' + escapeHtml(item.error);
            }).join('; ') + '.</p>');
        }

        var acted = false;
        var place = function (mode) {
            acted = true;
            finishImport(editor, items, errors, mode, bookmark);
        };
        var restoreCaret = function () {
            editor.focus();
            editor.selection.moveToBookmark(bookmark);
        };

        editor.windowManager.open({
            title: 'Add HTML',
            body: {
                type: 'panel',
                items: [{ type: 'htmlpanel', html: lines.join('') }]
            },
            buttons: [
                { type: 'cancel', text: 'Cancel' },
                { type: 'custom', name: 'replace', text: 'Replace everything' },
                { type: 'custom', name: 'append', text: 'Add at the end' },
                { type: 'submit', text: insertLabel, primary: true }
            ],
            onSubmit: function (api) {
                acted = true;
                api.close();
                place(selected ? 'selection' : 'insert');
            },
            onAction: function (api, details) {
                if (details.name === 'append') {
                    acted = true;
                    api.close();
                    place('append');
                    return;
                }
                if (details.name === 'replace') {
                    acted = true;
                    api.close();
                    editor.windowManager.confirm(
                        'Replace everything in the editor with ' + fileLabel(items) + '? You can undo this afterwards.',
                        function (ok) {
                            if (ok) place('replace');
                            else restoreCaret();
                        }
                    );
                }
            },
            onClose: function () {
                if (!acted) restoreCaret();
            }
        });
    }

    function importFiles(editor, files, dropEvent) {
        var list = Array.prototype.slice.call(files || []).filter(function (file) {
            return file && file.size >= 0;
        });
        if (!list.length) return;

        var overflow = [];
        if (list.length > MAX_HTML_FILES) {
            overflow = list.slice(MAX_HTML_FILES);
            list = list.slice(0, MAX_HTML_FILES);
        }

        // A drop chooses the caret. An existing selection is kept, so it can be replaced on purpose.
        if (dropEvent && editor.selection.isCollapsed() && typeof editor.selection.placeCaretAt === 'function') {
            try {
                editor.selection.placeCaretAt(dropEvent.clientX, dropEvent.clientY);
            } catch (e) {
                // Keep the existing caret when the drop point cannot be resolved.
            }
        }

        Promise.all(list.map(readHtmlFile)).then(function (results) {
            var items = [];
            var errors = results.filter(function (result) {
                if (result.html) {
                    items.push(result);
                    return false;
                }
                return true;
            });
            overflow.forEach(function (file) {
                errors.push({ name: file.name || 'A file', error: 'was left out (up to 10 files at a time)' });
            });

            if (!items.length) {
                notify(editor, errors.map(function (item) {
                    return item.name + ' ' + item.error + '.';
                }).join(' '), 'warning');
                return;
            }

            if (!editorHasContent(editor)) {
                finishImport(editor, items, errors, 'load', null);
                return;
            }

            openPlacementDialog(editor, items, errors);
        });
    }

    function dragHasFiles(event) {
        var transfer = event && event.dataTransfer;
        if (!transfer || !transfer.types) return false;
        return Array.prototype.indexOf.call(transfer.types, 'Files') !== -1;
    }

    function bindHtmlImport(editor) {
        var dragDepth = 0;

        editor.ui.registry.addButton('importhtml', {
            icon: 'upload-from-device',
            tooltip: 'Import HTML file',
            onAction: function () {
                chooseHtmlFiles().then(function (files) {
                    importFiles(editor, files);
                });
            }
        });

        editor.on('dragenter', function (event) {
            if (!dragHasFiles(event)) return;
            dragDepth++;
            editor.getBody().classList.add('import-html-hover');
        });
        editor.on('dragover', function (event) {
            if (!dragHasFiles(event)) return;
            event.preventDefault();
        });
        editor.on('dragleave', function (event) {
            if (!dragHasFiles(event)) return;
            dragDepth = Math.max(0, dragDepth - 1);
            if (dragDepth === 0) editor.getBody().classList.remove('import-html-hover');
        });
        editor.on('drop', function (event) {
            dragDepth = 0;
            var body = editor.getBody();
            if (body) body.classList.remove('import-html-hover');
            var transfer = event.dataTransfer;
            if (!transfer || !transfer.files || !transfer.files.length) return;
            var htmlFiles = Array.prototype.filter.call(transfer.files, isHtmlFile);
            if (!htmlFiles.length) return;
            event.preventDefault();
            if (event.stopPropagation) event.stopPropagation();
            importFiles(editor, htmlFiles, event);
        });
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
        toolbar: 'undo redo | blocks | bold italic inlinecode | bullist numlist blockquote | link image table | code importhtml',
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
        content_style: 'img{max-width:100%;height:auto}table,th,td{border:1px solid #6d737b}body.import-html-hover{box-shadow:inset 0 0 0 2px #4dc8ff}',
        setup: function (editor) {
            bindHtmlImport(editor);
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
