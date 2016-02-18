package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;
import com.faris.kingkits.helper.Debugger;

import java.sql.*;
import java.util.*;

public class StatementSelectTable extends StatementBuilder {

	private static String statementFormat = "SELECT %s FROM %s WHERE %s";

	private String columns = "*";
	private String table = null;
	private String where = "1";
	private int limit = -1;

	public StatementSelectTable(MySQLHandler handler) {
		super(handler);
	}

	public ResultSet execute() {
		Connection connection = this.sqlHandler.getConnection();
		if (connection != null) {
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			try {
				String sqlString = this.toSQLString();
				Debugger.debugMessage("SQL: " + sqlString);
				if (sqlString != null) {
					preparedStatement = connection.prepareStatement(sqlString);
					resultSet = preparedStatement.executeQuery();
					return resultSet;
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (preparedStatement != null) {
					try {
						preparedStatement.close();
					} catch (SQLException ignored) {
					}
				}
			}
		}
		return null;
	}

	public Table executeAsTable() {
		Connection connection = this.sqlHandler.getConnection();
		if (connection != null) {
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			try {
				String sqlString = this.toSQLString();
				Debugger.debugMessage("SQL: " + sqlString);
				if (sqlString != null) {
					preparedStatement = connection.prepareStatement(sqlString);
					resultSet = preparedStatement.executeQuery();
					return new Table(resultSet);
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (preparedStatement != null) {
					try {
						preparedStatement.close();
					} catch (SQLException ignored) {
					}
				}
				if (resultSet != null) {
					try {
						resultSet.close();
					} catch (SQLException ignored) {
					}
				}
			}
		}
		return null;
	}

	public StatementSelectTable setColumns(String... columns) {
		return this.setColumns(true, columns);
	}

	public StatementSelectTable setColumns(boolean addSpecialCharacters, String... columns) {
		if (columns.length == 0 || "*".equals(columns[0]) || "1".equals(columns[0])) {
			this.columns = columns.length == 0 ? "*" : columns[0];
		} else {
			StringBuilder sbColumns = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				if (columns[i] == null)
					throw new IllegalArgumentException("Column at index " + i + " is null in the array " + Arrays.toString(columns));
				if (addSpecialCharacters) sbColumns.append(this.getSpecialCharacter());
				sbColumns.append(columns[i]);
				if (addSpecialCharacters) sbColumns.append(this.getSpecialCharacter());
				sbColumns.append(", ");
			}
			this.columns = sbColumns.toString();
			if (this.columns.trim().length() == 0) this.columns = "*";
			else this.columns = this.columns.substring(0, this.columns.length() - 2);
		}
		return this;
	}

	public StatementSelectTable setRowLimit(int rowLimit) {
		this.limit = rowLimit;
		return this;
	}

	public StatementSelectTable setTable(String table) {
		this.table = table == null ? null : (table.equals("*") ? "*" : this.getSpecialCharacter() + table + this.getSpecialCharacter());
		return this;
	}

	public StatementSelectTable setWhere(String whereStatement) {
		this.where = whereStatement == null ? "1" : whereStatement;
		return this;
	}

	public StatementSelectTable setWhere(String column, Object value) {
		if (column == null) throw new IllegalArgumentException("Column cannot be null");
		return this.setWhere(this.getSpecialCharacter() + column + this.getSpecialCharacter() + "='" + (value == null ? "NULL" : value.toString()) + "'");
	}

	public StatementSelectTable setWhere(String column, String value, boolean caseInsensitive) {
		if (caseInsensitive) {
			if (column == null) throw new IllegalArgumentException("Column cannot be null");
			return this.setWhere("UPPER(" + this.getSpecialCharacter() + column + this.getSpecialCharacter() + ")=UPPER('" + (value == null ? "NULL" : value) + "')");
		} else {
			return this.setWhere(column, value);
		}
	}

	@Override
	public String toSQLString() {
		return this.table != null ? String.format(statementFormat, this.columns, this.table, this.where) + (this.limit != -1 ? " LIMIT " + Math.abs(this.limit) : "") + ";" : null;
	}

	public static class Table {
		private Map<Integer, String> idToColumn = null;
		private Map<Integer, List<Column>> rowIdToColumn = null;

		public Table(ResultSet resultSet) {
			this.idToColumn = new HashMap<>();
			this.rowIdToColumn = new LinkedHashMap<>();
			if (resultSet != null) {
				try {
					ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
					int columnCount = resultSetMetaData.getColumnCount();
					for (int i = 1; i < columnCount + 1; i++) {
						String columnName = resultSetMetaData.getColumnName(i);
						this.idToColumn.put(i - 1, columnName);
					}

					int row = 0;
					while (resultSet.next() && !resultSet.isAfterLast()) {
						List<Column> columnList = new ArrayList<>();
						for (Map.Entry<Integer, String> columnEntry : this.idToColumn.entrySet())
							columnList.add(new Column(columnEntry.getKey(), resultSet.getObject(columnEntry.getValue())));
						this.rowIdToColumn.put(row, columnList);
						row++;
					}
				} catch (Exception ex) {
					ex.printStackTrace();

					this.idToColumn.clear();
					this.rowIdToColumn.clear();
				}
			}
		}

		public Column getColumn(int rowIndex, String columnName) {
			List<Column> columns = this.rowIdToColumn.get(rowIndex);
			if (columns == null)
				throw new ArrayIndexOutOfBoundsException("Row index is out of range (Index = " + rowIndex + ", Size = " + this.rowIdToColumn.size() + ")");
			for (Column column : columns) {
				if (Objects.toString(this.idToColumn.get(column.getIndex())).equals(columnName)) return column;
			}
			return null;
		}

		public int getColumnCount() {
			return this.idToColumn.size();
		}

		public List<String> getColumns() {
			return new ArrayList<>(this.idToColumn.values());
		}

		public int getRowCount() {
			return this.rowIdToColumn.size();
		}

		public boolean hasColumn(String column) {
			return this.idToColumn.containsValue(column);
		}

		public boolean isEmpty() {
			return this.rowIdToColumn.isEmpty();
		}

		@Override
		public String toString() {
			StringBuilder sbTable = new StringBuilder();

			List<String> columns = this.getColumns();
			for (int i = 0; i < columns.size(); i++) {
				if (i == 0) sbTable.append("| ");
				sbTable.append(columns.get(i)).append(" |");
				if (i != columns.size() - 1) sbTable.append(" ");
			}
			if (!this.rowIdToColumn.isEmpty()) sbTable.append(System.lineSeparator());

			for (Map.Entry<Integer, List<Column>> rowEntry : this.rowIdToColumn.entrySet()) {
				for (int i = 0; i < rowEntry.getValue().size(); i++) {
					if (i == 0) sbTable.append("| ");
					sbTable.append(rowEntry.getValue().get(i).asString()).append(" |");
					if (i < rowEntry.getValue().size() - 1) sbTable.append(" ");
				}
				sbTable.append(System.lineSeparator());
			}

			return sbTable.toString();
		}

		public static class Column {
			private int columnIndex = -1;
			private Object columnValue = null;

			public Column(int index, Object value) {
				this.columnIndex = index;
				this.columnValue = value;
			}

			public double asDouble() {
				if (this.columnValue instanceof Double) return (Double) this.columnValue;
				return Double.valueOf(this.asString());
			}

			public float asFloat() {
				if (this.columnValue instanceof Float) return (Float) this.columnValue;
				return Float.valueOf(this.asString());
			}

			public int asInteger() {
				if (this.columnValue instanceof Integer) return (Integer) this.columnValue;
				return Integer.valueOf(this.asString());
			}

			public long asLong() {
				if (this.columnValue instanceof Long) return (Long) this.columnValue;
				return Long.valueOf(this.asString());
			}

			public Number asNumber() {
				if (this.columnValue instanceof Number) return (Number) this.columnValue;
				return null;
			}

			public String asString() {
				if (this.columnValue instanceof String) return (String) this.columnValue;
				return Objects.toString(this.getValue());
			}

			public int getIndex() {
				return this.columnIndex;
			}

			public Object getValue() {
				return this.columnValue;
			}

			public boolean isNull() {
				return this.getValue() == null;
			}

			@Override
			public String toString() {
				return "{" + this.columnIndex + "=" + this.asString() + "}";
			}
		}
	}

}
