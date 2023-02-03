import styled from "styled-components";
import RoomHeader from "../../layout/room/RoomHeader";
import RoomMain from "../../layout/room/RoomMain";
import React, { useState, useEffect } from "react";
import { useSelector } from "react-redux";
import useAxios from "../../../action/hooks/useAxios";
import { useLocation } from "react-router-dom";
import { useParams } from "react-router-dom";

function RoomPage() {
  // roomId 값을 RoomListItem에서 Link state에 받아와서
  // useLocation에 넣어논 roomId 값을 가져와서 사용함
  const location = useLocation();
 const params = useParams();
 const roomParamsId = params.id

  
 const password = location.state.password
  const [join, setJoin] = useState(false);

  // true: 대가자
  // false: 참여자

  useEffect(() => {
  }, [join]);

  const joinRoom = (join) => {
    setJoin(join);
  };


  const [host, setHost] = useState(false);

  // 방장 권한을 어떤 방식으로 주는지 감이 안와서
  //일단 임시로 설정해 놓았습니다.
  // true : 방장 -> 방없애기 방 수정하기 버튼 활성화
  // false: 대기자 -> 참여하기 버튼 활성화

  // 방장 판별 슈도코드
  // const isHost = () =>{
  //   if 아이디 값 == 방 생성 아이디
  //     host 값 true
  //   else 아이디 값 !== 방생성 아이디
  //    host 값 false
  // }

  const [valid ,setValid] = useState({
    "password":password
  })

  const token = useSelector((state) => state.token);
  const [data, setData] = useState();

  const [postData, isLoading] = useAxios(
    `interviewrooms/${roomParamsId}`,
    "POST",
    token,
    valid
  );

  useEffect(() => {
    if (postData && postData.data) {
      setData(postData.data);
      console.log(postData.data);
    }
  }, [postData]);

  const [myId, loading] = useAxios(
    'userinfo',
    "GET",
    token
  )

  useEffect (() => {
    if (postData && postData.data && postData.data.manager?.id === myId?.data.id ){
      setHost(true)
    }
  },[postData,myId])

  

  return (
    <Container>
      <Room>
        {
          isLoading === true ? <div>loading...</div> :
          data && data.id ?
          <>
            <RoomHeader joinRoom={joinRoom} data={data} join={join} host={host} />
            <RoomMain data={data} join={join} host={host} />
          </> : <div>없는디여</div>
        }
      </Room>
    </Container>
  );
}
export default RoomPage;

const Room = styled.div`
  margin-left: 10%;
  margin-right: 10%;
  width: 80vw;
  display: grid;
  grid-template-columns: 1fr 3fr;
  align-items: flex-start;
`;

const Container = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: center;
  padding-top: 10vh;
  background-color: var(--white);
`
