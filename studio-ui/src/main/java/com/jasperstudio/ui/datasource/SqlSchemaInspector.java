package com.jasperstudio.ui.datasource;

import com.jasperstudio.adapter.JdbcDataAdapter;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SqlSchemaInspector {
    private static final Logger logger = LoggerFactory.getLogger(SqlSchemaInspector.class);

    public static TreeItem<String> inspect(JdbcDataAdapter adapter) {
        TreeItem<String> root = new TreeItem<>(adapter.getName());
        root.setExpanded(true);

        try (Connection conn = adapter.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            // Get Tables
            try (ResultSet tables = meta.getTables(null, null, "%", new String[] { "TABLE" })) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    TreeItem<String> tableNode = new TreeItem<>(tableName);

                    // Get Columns for Table
                    try (ResultSet columns = meta.getColumns(null, null, tableName, "%")) {
                        while (columns.next()) {
                            String colName = columns.getString("COLUMN_NAME");
                            // String colType = columns.getString("TYPE_NAME");
                            TreeItem<String> colNode = new TreeItem<>(colName);
                            tableNode.getChildren().add(colNode);
                        }
                    }
                    root.getChildren().add(tableNode);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to inspect schema", e);
            root.getChildren().add(new TreeItem<>("Error: " + e.getMessage()));
        }

        return root;
    }
}
