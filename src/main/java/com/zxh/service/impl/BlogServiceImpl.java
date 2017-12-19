package com.zxh.service.impl;

import com.zxh.dao.BlogRepository;
import com.zxh.exception.NotFoundException;
import com.zxh.model.Blog;
import com.zxh.model.Type;
import com.zxh.service.BlogService;
import com.zxh.util.MarkdownUtils;
import com.zxh.util.MyBeanUtils;
import com.zxh.vo.BlogQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/12/16.
 */

@Service
public class BlogServiceImpl implements BlogService {
    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    @Autowired
    private BlogRepository blogRepository;

    @Override
    public Blog getBlog(Long id) {
        return blogRepository.findByIdAndDeleteFlag(id, false);
    }

    @Override
    public Page<Blog> listBlog(Pageable pageable, BlogQuery blog) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (!"".equals(blog.getTitle()) && blog.getTitle() != null) {
                    predicates.add(cb.like(root.<String>get("title"), "%"+blog.getTitle()+"%"));
                }
                if (blog.getTypeId() != null) {
                    predicates.add(cb.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
                }
                if (blog.isRecommend()) {
                    predicates.add(cb.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                //predicates.add(cb.equal(root.<Boolean>get("deleteFlag"), false));
                cq.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        }, pageable);
    }

    @Override
    public Page<Blog> listBlog(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public Blog saveBlog(Blog blog) {
        if(blog.getId() == null) {
            //可以在数据库设置默认值和触发器
            blog.setCreateTime(new Date());
            blog.setUpdateTime(new Date());
            blog.setViews(0);
        } else {
            blog.setUpdateTime(new Date());
        }

        return blogRepository.save(blog);
    }


    @Transactional
    @Override
    public Blog updateBlog(Long id, Blog blog) {
        Blog blog1 = blogRepository.findOne(id);
        if(blog1 == null) {
            logger.error("BlogServiceImpl.updateBlog.ERROR.不存在此id的blog, id: {}", id);
            throw new NotFoundException("该博客不存在");
        }
        //防止为空的属性覆盖了数据库。如view这个字段前端没有传过来，我们需要把它忽略
        BeanUtils.copyProperties(blog, blog1, MyBeanUtils.getNullPropertyName(blog));
        blog1.setUpdateTime(new Date());//可在数据库设置触发器
        return blogRepository.save(blog1);
    }

    @Transactional
    @Override
    public void deleteBlog(Long id) {
        Blog blog = blogRepository.findOne(id);
        if(blog == null) {
            logger.error("BlogServiceImpl.deleteBlog.ERROR.blog is not exist. id: {}", id);
        }
        blog.setDeleteFlag(true);
        blogRepository.save(blog);
    }

    @Override
    public List<Blog> listBlog() {
        return blogRepository.findAll();
    }

    @Override
    public List<Blog> listReCommendBlogTop(Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        Pageable pageable = new PageRequest(0, size, sort);
        return blogRepository.findReCommendTop(pageable);
    }

    @Override
    public List<Blog> listBlogTop(Integer size) {
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        Pageable pageable = new PageRequest(0, size, sort);
        return blogRepository.findTop(pageable);
    }

    @Override
    public Page<Blog> listPage(String query, Pageable pageable) {
        query = "%" + query + "%";//实现模糊查询
        return blogRepository.findByQuery(query, pageable);
    }

    @Override
    public Blog getAndConvert(Long id) {
        Blog blog = blogRepository.findOne(id);
        if(blog == null) {
            throw new NotFoundException("该博客不存在");
        }

        //创建一个新的Blog对象，对该新对象进行Markdown格式转换并返回给前端，防止对数据库的数据修改
        Blog blog1 = new Blog();
        BeanUtils.copyProperties(blog, blog1);
        blog1.setContent(MarkdownUtils.markdownToHtmlExtensions(blog.getContent()));
        //将views+1
        blogRepository.updateViews(id);
        return blog1;
    }
}
