package com.mall.tinymall.util;

import com.mall.tinymall.entity.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB 通用操作工具类（适配 Comment 实体 + Spring Boot 3.4.4）
 * 优化点：兼容 Integer 类型ID、支持列表字段操作、补充评论业务高频方法
 */
@Component
public class MongoDbUtils {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoDbUtils(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // ===================== 基础 CRUD 方法（兼容 Integer ID） =====================
    /**
     * 新增单个文档（适配 Comment 类的 Integer id）
     * @param entity 实体对象（Comment 或其他实体）
     * @param <T> 实体类型
     * @return 新增后的实体
     */
    public <T> T save(T entity) {
        try {
            return mongoTemplate.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("新增文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据 ID 查询（兼容 Integer/String 类型 ID）
     * @param id 文档 ID（Comment 的 id 是 Integer）
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 封装后的 Optional
     */
    public <T> Optional<T> findById(Object id, Class<T> entityClass) {
        try {
            T entity = mongoTemplate.findById(id, entityClass);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new RuntimeException("根据ID查询文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查询指定集合的所有文档
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 文档列表
     */
    public <T> List<T> findAll(Class<T> entityClass) {
        try {
            return mongoTemplate.findAll(entityClass);
        } catch (Exception e) {
            throw new RuntimeException("查询所有文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询单个文档
     * @param criteria 查询条件
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 封装后的 Optional
     */
    public <T> Optional<T> findOne(Criteria criteria, Class<T> entityClass) {
        try {
            Query query = new Query(criteria);
            T entity = mongoTemplate.findOne(query, entityClass);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new RuntimeException("条件查询单个文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据条件查询文档列表
     * @param criteria 查询条件
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 文档列表
     */
    public <T> List<T> findList(Criteria criteria, Class<T> entityClass) {
        try {
            Query query = criteria == null ? new Query() : new Query(criteria);
            return mongoTemplate.find(query, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("条件查询文档列表失败：" + e.getMessage(), e);
        }
    }

    // ===================== 适配 Comment 的专用扩展方法 =====================
    /**
     * 【快捷方法】根据 goodsId 查询所有评论（Comment 高频场景）
     * @param goodsId 商品ID
     * @return 评论列表
     */
    public List<Comment> findCommentsByGoodsId(Integer goodsId) {
        Criteria criteria = Criteria.where("goodsId").is(goodsId);
        return findList(criteria, Comment.class);
    }

    /**
     * 【快捷方法】根据 goodsId + 评分筛选评论
     * @param goodsId 商品ID
     * @param star 评分（如 5 分好评）
     * @return 评论列表
     */
    public List<Comment> findCommentsByGoodsIdAndStar(Integer goodsId, Integer star) {
        Criteria criteria = Criteria.where("goodsId").is(goodsId)
                .and("star").is(star);
        return findList(criteria, Comment.class);
    }

    /**
     * 【列表字段操作】给指定评论追加图片（Comment 的 images 字段）
     * @param commentId 评论ID（Integer 类型）
     * @param imageUrl 图片URL
     * @return 受影响的行数
     */
    public long addCommentImage(Integer commentId, String imageUrl) {
        try {
            Criteria criteria = Criteria.where("_id").is(commentId);
            Update update = new Update().push("images", imageUrl); // push 向数组追加元素
            return mongoTemplate.updateFirst(query(criteria), update, Comment.class).getModifiedCount();
        } catch (Exception e) {
            throw new RuntimeException("追加评论图片失败：" + e.getMessage(), e);
        }
    }

    /**
     * 【批量操作】根据 goodsId 批量删除评论
     * @param goodsId 商品ID
     * @return 受影响的行数
     */
    public long deleteCommentsByGoodsId(Integer goodsId) {
        Criteria criteria = Criteria.where("goodsId").is(goodsId);
        return delete(criteria, Comment.class);
    }

    // ===================== 通用分页/更新/删除方法（兼容 Integer ID） =====================
    /**
     * 分页 + 排序 + 条件查询
     * @param criteria 查询条件
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页条数
     * @param sort 排序规则
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 分页结果
     */
    public <T> Page<T> findPage(Criteria criteria, int pageNum, int pageSize, Sort sort, Class<T> entityClass) {
        try {
            Query query = criteria == null ? new Query() : new Query(criteria);
            long total = mongoTemplate.count(query, entityClass);
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
            query.with(pageable);
            List<T> content = mongoTemplate.find(query, entityClass);
            return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            throw new RuntimeException("分页查询文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据 ID 更新（兼容 Integer ID）
     * @param id 文档 ID
     * @param entity 新的实体对象
     * @param <T> 实体类型
     * @return 更新后的实体
     */
    public <T> T updateById(Object id, T entity) {
        try {
            return mongoTemplate.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("根据ID更新文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 条件更新
     * @param criteria 更新条件
     * @param update 更新字段
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> long update(Criteria criteria, Update update, Class<T> entityClass) {
        try {
            Query query = new Query(criteria);
            return mongoTemplate.updateFirst(query, update, entityClass).getModifiedCount();
        } catch (Exception e) {
            throw new RuntimeException("条件更新文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 批量更新
     * @param criteria 更新条件
     * @param update 更新字段
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> long batchUpdate(Criteria criteria, Update update, Class<T> entityClass) {
        try {
            Query query = new Query(criteria);
            return mongoTemplate.updateMulti(query, update, entityClass).getModifiedCount();
        } catch (Exception e) {
            throw new RuntimeException("批量更新文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据 ID 删除（兼容 Integer ID）
     * @param id 文档 ID
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     */
    public <T> void deleteById(Object id, Class<T> entityClass) {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), entityClass);
        } catch (Exception e) {
            throw new RuntimeException("根据ID删除文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 条件删除
     * @param criteria 删除条件
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> long delete(Criteria criteria, Class<T> entityClass) {
        try {
            Query query = new Query(criteria);
            return mongoTemplate.remove(query, entityClass).getDeletedCount();
        } catch (Exception e) {
            throw new RuntimeException("条件删除文档失败：" + e.getMessage(), e);
        }
    }

    /**
     * 统计符合条件的文档数量
     * @param criteria 查询条件
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return 文档数量
     */
    public <T> long count(Criteria criteria, Class<T> entityClass) {
        try {
            Query query = criteria == null ? new Query() : new Query(criteria);
            return mongoTemplate.count(query, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("统计文档数量失败：" + e.getMessage(), e);
        }
    }

    /**
     * 判断文档是否存在
     * @param criteria 查询条件
     * @param entityClass 实体类 Class
     * @param <T> 实体类型
     * @return true：存在，false：不存在
     */
    public <T> boolean exists(Criteria criteria, Class<T> entityClass) {
        try {
            Query query = new Query(criteria);
            return mongoTemplate.exists(query, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("判断文档是否存在失败：" + e.getMessage(), e);
        }
    }

    // 私有工具方法：简化 Query 创建
    private Query query(Criteria criteria) {
        return new Query(criteria);
    }
}