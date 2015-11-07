package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;

public abstract class StatementBuilder {

	protected final MySQLHandler sqlHandler;

	public StatementBuilder(MySQLHandler handler) {
		this.sqlHandler = handler;
	}

	public abstract String toSQLString();

	protected char getSpecialCharacter() {
		return '`';
	}

	protected String stripSpecialCharacters(String aString) {
		return aString != null ? aString.replace(String.valueOf(this.getSpecialCharacter()), "") : null;
	}

	public static char getTableAndColumnCharacter() {
		return '`';
	}

}
