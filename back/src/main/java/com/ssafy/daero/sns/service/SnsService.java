package com.ssafy.daero.sns.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.daero.sns.mapper.SnsMapper;
import com.ssafy.daero.sns.vo.ArticleVo;
import com.ssafy.daero.sns.vo.ReplyVo;
import com.ssafy.daero.sns.vo.StampVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SnsService {
    private final SnsMapper snsMapper;

    public SnsService(SnsMapper snsMapper) { this.snsMapper = snsMapper; }

    public Map<String, Object> articleDetail(int articleSeq) throws JsonProcessingException {

        ArticleVo articleVo = snsMapper.selectArticleAndTripInfoByArticleSeq(articleSeq);
        ArrayList<StampVo> stampVo = snsMapper.selectStampAndDayInfoByTripSeq(articleVo.getTripSeq());
        Map<String, String> userInfo = snsMapper.selectUserByUserSeq(articleVo.getUserSeq());
        ArrayList<Integer> tags = snsMapper.selectPlaceTagsByArticleSeq(articleSeq);
        Map<String, Object> articleDetail = new HashMap<>();

        ArrayList<Map> records = new ArrayList<>();
        Map<String, Object> days = new HashMap<>();
        ArrayList<Map> stamps = new ArrayList<>();
        Map<String, Object> stamp = new HashMap<>();

        int currentDaySeq = stampVo.get(0).getTripDaySeq();
        String datetime = stampVo.get(0).getDatetime();
        String dayComment = stampVo.get(0).getDayComment();
        for (StampVo sVo :
             stampVo) {
            if (sVo.getTripDaySeq() != currentDaySeq) {
                days.put("datetime", datetime);
                days.put("day_comment", dayComment);
                days.put("trip_stamps", stamps);
                records.add(days);

                currentDaySeq = sVo.getTripDaySeq();
                datetime = sVo.getDatetime();
                dayComment = sVo.getDayComment();
                stamps = new ArrayList<>();
                days = new HashMap<>();
            }
            stamp.put("image_url", sVo.getImageUrl());
            stamp.put("trip_stamp_seq", sVo.getTripStampSeq());
            stamp.put("latitude", sVo.getLatitude());
            stamp.put("longitude", sVo.getLongitude());
            stamps.add(stamp);
            stamp = new HashMap<>();

        }
        days.put("datetime", datetime);
        days.put("day_comment", dayComment);
        days.put("trip_stamps", stamps);
        records.add(days);

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Map> expenses = mapper.readValue(articleVo.getTripExpenses(), ArrayList.class);

        articleDetail.put("user_seq", articleVo.getUserSeq());
        articleDetail.put("nickname", userInfo.get("nickname"));
        articleDetail.put("profile_url", userInfo.get("profile_image_link"));
        articleDetail.put("title", articleVo.getTitle());
        articleDetail.put("trip_comment", articleVo.getTripComment());
        articleDetail.put("rating", articleVo.getRating());
        articleDetail.put("likes", articleVo.getLikes());
        articleDetail.put("comments", articleVo.getComments());
        articleDetail.put("tags", tags);
        articleDetail.put("trip_expenses", expenses);
        articleDetail.put("records", records);
        return articleDetail;
    }

    public Integer deleteArticle(int articleSeq, int userSeq) {
        // 본인 게시글인지 확인
        Integer articleUser = snsMapper.selectUserSeqByArticleSeq(articleSeq);
        if (articleUser == null) { return 0; }
        if (articleUser == userSeq) {
            snsMapper.deleteArticleByArticleSeq(articleSeq);
            return 1;
        }
        else {
            return 99;
        }
    }

    public ArrayList<Map<String, Object>> replyList(int articleSeq, String page) {
        Integer articleUser = snsMapper.selectUserSeqByArticleSeq(articleSeq);
        if (articleUser == null) { return null; }

        ArrayList<ReplyVo> replyVos = snsMapper.selectReplyListByArticleSeq(articleSeq, Integer.parseInt(page));
        ArrayList<Map<String, Object>> replyList = new ArrayList<>();
        Map<String, Object> reply = new HashMap<>();
        for (ReplyVo rVo :
                replyVos) {
            reply.put("reply_seq", rVo.getReplySeq());
            reply.put("nickname", rVo.getNickname());
            reply.put("user_seq", rVo.getUserSeq());
            reply.put("profile_url", rVo.getProfileUrl());
            reply.put("created_at", rVo.getCreatedAt());
            reply.put("content", rVo.getContent());
            reply.put("rereply_count", rVo.getRereplyCount());
            if (Objects.equals(rVo.getCreatedAt(), rVo.getUpdatedAt())) { reply.put("modified", 'n'); }
            else { reply.put("modified", 'y'); }
            replyList.add(reply);
            reply = new HashMap<>();
        }
        return replyList;
    }

    public ReplyVo createReply(int articleSeq, int userSeq, String content) {
        ReplyVo replyVo = new ReplyVo();
        // article이 존재하는지 확인
        int article = snsMapper.selectArticleByArticleSeq(articleSeq);
        if (article == 0) { replyVo.setResult(ReplyVo.ReplyResult.NO_SUCH_ARTICLE); return replyVo; }
        else {
            snsMapper.insertReply(articleSeq, userSeq, content);
            replyVo.setResult(ReplyVo.ReplyResult.SUCCESS);
            return replyVo;
        }
    }

    public ReplyVo updateReply(int userSeq, int replySeq, String content) {
        Integer replyUser = snsMapper.selectUserSeqByReplySeq(replySeq);
        ReplyVo replyVo = new ReplyVo();
        // reply가 없는 경우
        if (replyUser == null) { replyVo.setResult(ReplyVo.ReplyResult.NO_SUCH_REPLY); return replyVo;}
        // replyUser와 userSeq가 다른 경우(본인이 아닌 경우) (UNAUTH)
        if (replyUser != userSeq) { replyVo.setResult(ReplyVo.ReplyResult.UNAUTHORIZED); return replyVo;}
        // 본인이 맞는 경우 -> 수정
        snsMapper.updateReplyByReplySeq(replySeq, content);
        replyVo.setResult(ReplyVo.ReplyResult.SUCCESS);
        return replyVo;
    }

    public ReplyVo deleteReply(int userSeq, int replySeq) {
        Integer replyUser = snsMapper.selectUserSeqByReplySeq(replySeq);
        ReplyVo replyVo = new ReplyVo();
        // reply가 없는 경우
        if (replyUser == null) { replyVo.setResult(ReplyVo.ReplyResult.NO_SUCH_REPLY); return replyVo;}
        // replyUser와 userSeq가 다른 경우(본인이 아닌 경우) (UNAUTH)
        if (replyUser != userSeq) { replyVo.setResult(ReplyVo.ReplyResult.UNAUTHORIZED); return replyVo;}
        snsMapper.deleteReplyByReplySeq(replySeq);
        replyVo.setResult(ReplyVo.ReplyResult.SUCCESS);
        return replyVo;

    }
}