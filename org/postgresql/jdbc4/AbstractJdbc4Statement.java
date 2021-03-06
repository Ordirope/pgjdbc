/*-------------------------------------------------------------------------
*
* Copyright (c) 2004-2014, PostgreSQL Global Development Group
*
*
*-------------------------------------------------------------------------
*/
package org.postgresql.jdbc4;

import java.sql.*;
import java.io.Reader;
import java.io.InputStream;

import org.postgresql.core.Oid;
import org.postgresql.jdbc2.ResultWrapper;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLState;
import org.postgresql.util.PSQLException;

abstract class AbstractJdbc4Statement extends org.postgresql.jdbc3g.AbstractJdbc3gStatement
{

    private boolean poolable;
    private boolean closeOnCompletion = false;

    AbstractJdbc4Statement (Jdbc4Connection c, int rsType, int rsConcurrency, int rsHoldability) throws SQLException
    {
        super(c, rsType, rsConcurrency, rsHoldability);
        poolable = true;
    }

    public AbstractJdbc4Statement(Jdbc4Connection connection, String sql, boolean isCallable, int rsType, int rsConcurrency, int rsHoldability) throws SQLException
    {
        super(connection, sql, isCallable, rsType, rsConcurrency, rsHoldability);
    }

    public boolean isClosed() throws SQLException
    {
        return isClosed;
    }

    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        if (x instanceof SQLXML)
        {
            setSQLXML(parameterIndex, (SQLXML)x);
        } else {
            super.setObject(parameterIndex, x);
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException
    {
        checkClosed();

        if (x == null)
        {
            setNull(parameterIndex, targetSqlType);
            return;
        }

        switch (targetSqlType) {
            case Types.SQLXML:
                if (x instanceof SQLXML) {
                    setSQLXML(parameterIndex, (SQLXML)x);
                } else {
                    setSQLXML(parameterIndex, new Jdbc4SQLXML(connection, x.toString()));
                }
                break;
            default:
                super.setObject(parameterIndex, x, targetSqlType, scale);
        }
    }

    public void setNull(int parameterIndex, int targetSqlType) throws SQLException
    {
        checkClosed();
        int oid;
        switch (targetSqlType)
        {
            case Types.SQLXML:
                oid = Oid.XML;
                break;
            default:
                super.setNull(parameterIndex, targetSqlType);
                return;
        }

        if (adjustIndex)
            parameterIndex--;
        preparedParameters.setNull(parameterIndex, oid);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setRowId(int, RowId)");
    }

    public void setNString(int parameterIndex, String value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNString(int, String)");
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader, long)");
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNCharacterStream(int, Reader)");
    }

    public void setCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setCharacterStream(int, Reader, long)");
    }

    public void setCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setCharacterStream(int, Reader)");
    }

    public void setBinaryStream(int parameterIndex, InputStream value, long length) throws SQLException
    {
	if (length > Integer.MAX_VALUE)
	{
	    throw new PSQLException(GT.tr("Object is too large to send over the protocol."), PSQLState.NUMERIC_CONSTANT_OUT_OF_RANGE);
	}	
        preparedParameters.setBytea(parameterIndex, value, (int)length);
    }

    public void setBinaryStream(int parameterIndex, InputStream value) throws SQLException
    {
        preparedParameters.setBytea(parameterIndex, value);
    }

    public void setAsciiStream(int parameterIndex, InputStream value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream, long)");
    }

    public void setAsciiStream(int parameterIndex, InputStream value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setAsciiStream(int, InputStream)");
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(int, NClob)");
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setClob(int, Reader, long)");
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setClob(int, Reader)");
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
    {
        checkClosed();

        if (inputStream == null)
        {
            setNull(parameterIndex, Types.BLOB);
            return;
        }

        if (length < 0)
        {
            throw new PSQLException(GT.tr("Invalid stream length {0}.", new Long(length)),
                                    PSQLState.INVALID_PARAMETER_VALUE);
        }

        long oid = createBlob(parameterIndex, inputStream, length);
        setLong(parameterIndex, oid);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        checkClosed();

        if (inputStream == null)
        {
            setNull(parameterIndex, Types.BLOB);
            return;
        }

        long oid = createBlob(parameterIndex, inputStream, -1);
        setLong(parameterIndex, oid);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(int, Reader, long)");
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(int, Reader)");
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        checkClosed();
        if (xmlObject == null || xmlObject.getString() == null)
            setNull(parameterIndex, Types.SQLXML);
        else
            setString(parameterIndex, xmlObject.getString(), Oid.XML);
    }

    public void setPoolable(boolean poolable) throws SQLException
    {
        checkClosed();
        this.poolable = poolable;
    }

    public boolean isPoolable() throws SQLException
    {
        checkClosed();
        return poolable;
    }

    public RowId getRowId(int parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getRowId(int)");
    }

    public RowId getRowId(String parameterName) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getRowId(String)");
    }

    public void setRowId(String parameterName, RowId x) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setRowId(String, RowId)");
    }
    
    public void setNString(String parameterName, String value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNString(String, String)");
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNCharacterStream(String, Reader, long)");
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNCharacterStream(String, Reader)");
    }

    public void setCharacterStream(String parameterName, Reader value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setCharacterStream(String, Reader, long)");
    }

    public void setCharacterStream(String parameterName, Reader value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setCharacterStream(String, Reader)");
    }

    public void setBinaryStream(String parameterName, InputStream value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setBinaryStream(String, InputStream, long)");
    }

    public void setBinaryStream(String parameterName, InputStream value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setBinaryStream(String, InputStream)");
    }

    public void setAsciiStream(String parameterName, InputStream value, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setAsciiStream(String, InputStream, long)");
    }

    public void setAsciiStream(String parameterName, InputStream value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setAsciiStream(String, InputStream)");
    }

    public void setNClob(String parameterName, NClob value) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(String, NClob)");
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setClob(String, Reader, long)");
    }

    public void setClob(String parameterName, Reader reader) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setClob(String, Reader)");
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setBlob(String, InputStream, long)");
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setBlob(String, InputStream)");
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(String, Reader, long)");
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setNClob(String, Reader)");
    }

    public NClob getNClob(int parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNClob(int)");
    }

    public NClob getNClob(String parameterName) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNClob(String)");
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setSQLXML(String, SQLXML)");
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException
    {
        checkClosed();
        checkIndex(parameterIndex, Types.SQLXML, "SQLXML");
        return (SQLXML)callResult[parameterIndex - 1];
    }

    public SQLXML getSQLXML(String parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getSQLXML(String)");
    }

    public String getNString(int parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNString(int)");
    }

    public String getNString(String parameterName) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNString(String)");
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNCharacterStream(int)");
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getNCharacterStream(String)");
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getCharacterStream(int)");
    }

    public Reader getCharacterStream(String parameterName) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getCharacterStream(String)");
    }

    public void setBlob(String parameterName, Blob x) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setBlob(String, Blob)");
    }

    public void setClob(String parameterName, Clob x) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "setClob(String, Clob)");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        if (iface.isAssignableFrom(getClass()))
        {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getParentLogger()");
    }

    public void closeOnCompletion() throws SQLException
    {
        closeOnCompletion = true;
    }

    public boolean isCloseOnCompletion() throws SQLException
    {
        return closeOnCompletion;
    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getObject(int, Class<T>)");
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException
    {
        throw org.postgresql.Driver.notImplemented(this.getClass(), "getObject(String, Class<T>)");
    }

    protected void checkCompletion() throws SQLException
    {
        if (!closeOnCompletion)
            return;

        ResultWrapper result = firstUnclosedResult;
        while (result != null)
        {
            if (result.getResultSet() != null && !result.getResultSet().isClosed())
            {
                return;
            }
            result = result.getNext();
        }

        // prevent all ResultSet.close arising from Statement.close to loop here
        closeOnCompletion = false;
        try
        {
            close();
        }
        finally
        {
            // restore the status if one rely on isCloseOnCompletion
            closeOnCompletion = true;
        }
    }
}
