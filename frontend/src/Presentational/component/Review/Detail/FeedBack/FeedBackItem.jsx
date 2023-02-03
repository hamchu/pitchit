import styled from "styled-components";
import SubTitle from "../../../../common/SubTitle";

const FeedBackItem = ({data}) => {
    return (
        <FeedBack>
            <FeedBackTitle>
              <SubTitle title={data.writer.name}></SubTitle>
              <SubTitle title={data.score}></SubTitle>
            </FeedBackTitle>
            <SubTitle title={data.content}></SubTitle>
        </FeedBack>
    )
}

const FeedBack = styled.div`
  width: 100%;
  & > div:nth-child(2) {
    text-indent: 20px;
  }
  margin-bottom: 50px;
`

const FeedBackTitle = styled.div`
  display: flex;
  width: 100%;
  margin-bottom: 10px;

  & > div:first-child{
    font-weight: bold;
    font-size: 25px;
    margin-right: 25px;
  }

  & > div:nth-child(2){
    font-weight: bold;
    font-size: 25px;
  }
`

export default FeedBackItem;