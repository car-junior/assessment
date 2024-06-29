package com.senior.assessment.config;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
//@Profile("test")
public class DatabaseCleanupService implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;
    private String juncaoTabelas;

    private final Pattern regexSnakeCase = Pattern.compile("(?<=[a-zA-Z])[A-Z]");

    @Override
    public void afterPropertiesSet() throws Exception {
        var nomeTabelas = getManagedTableNames();
        juncaoTabelas = String.join(",", nomeTabelas);
    }

    private List<String> getManagedTableNames() {
        var metaModel = entityManager.getMetamodel();
        return metaModel.getManagedTypes()
                .stream()
                .filter(entityType -> {
                    var typeClass = entityType.getJavaType();
                    return typeClass.getAnnotation(Table.class) != null ||
                            typeClass.getAnnotation(Entity.class) != null;
                })
                .map(managedType -> {
                    var annotationName = getAnnotationName(managedType);
                    var schema = "";
                    if (!"".equals(managedType.getJavaType().getAnnotation(Table.class).schema()))
                        schema = managedType.getJavaType().getAnnotation(Table.class).schema();

                    return schema.concat(".").concat(getTableName(annotationName, managedType.getJavaType().getSimpleName()));
                })
                .toList();
    }

    private String getAnnotationName(ManagedType<?> managedType) {
        var typeClass = managedType.getJavaType();
        if (typeClass.getAnnotation(Table.class) != null)
            return typeClass.getAnnotation(Table.class).name();
        else
//            if (typeClass.getAnnotation(Entity.class) != null)
            return typeClass.getAnnotation(Entity.class).name();
    }

    private String getTableName(String annotationName, String javaTypeName) {
        if (Objects.equals(annotationName, "")) {
            return camelToSnakeCase(javaTypeName);
        } else {
            return annotationName;
        }
    }


    private String camelToSnakeCase(String camelString) {
        var matcher = regexSnakeCase.matcher(camelString);
        StringBuilder sb = new StringBuilder();

        // Substitui cada correspondência pela versão snake_case
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + "_" + matcher.group(2).toLowerCase(Locale.getDefault()));
        }
        matcher.appendTail(sb);

        return sb.toString().toLowerCase(Locale.getDefault());
    }

    @Transactional
    public void truncate() {
        entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s CASCADE", juncaoTabelas))
                .executeUpdate();
    }

}
