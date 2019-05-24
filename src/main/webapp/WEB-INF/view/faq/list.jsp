<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="EUC-KR"%>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>FAQ LIST</title>
</head>
<body>
<form name="searchBtn" method="get">
    <a href ="/faq/list?searchBtn=${cri.searchBtn = '����'}">����</a>
    <a href ="/faq/list?searchBtn=${cri.searchBtn = '�α���'}">�α���</a>
    <a href ="/faq/list?searchBtn=${cri.searchBtn = '��Ÿ����'}">��Ÿ����</a> 
</form>
    
    <table border="1">
        <tr>
            <th>����</th>
            <th>����</th>
            <th>�ۼ���</th>
            <th>��ȸ��</th>
        </tr>
        <c:forEach var="faq" items="${faqList}">
			<tr>
				<td>${faq.faqCategory}</td>
				<td><a href="/faq/list${pageMaker.makeSearch(pageMaker.cri.page)}&number=${faq.faqNum}">${faq.faqTitle}</a></td>
				<td><fmt:formatDate value="${faq.faqDate}" pattern="yyyy�� MM�� dd��"/></td>
				<td>${faq.faqHits}</td>
			</tr>
		</c:forEach>

    </table>
    
    <a class="btn btn-primary" href="/faq/write">�۾���</a>
  
</body>
</html>
